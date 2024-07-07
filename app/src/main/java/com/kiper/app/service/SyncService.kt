package com.kiper.app.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.kiper.app.receiver.AlarmReceiver
import com.kiper.core.data.Schedule
import com.kiper.core.data.repository.Repository
import com.kiper.core.framework.worker.AudioRecordWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class SyncService : Service() {

    @Inject
    lateinit var workManager: WorkManager

    @Inject
    lateinit var repository: Repository

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        println("SyncService onCreate")
        startForeground(NOTIFICATION_ID, getNotification())
        fetchDeviceSchedules()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        fetchDeviceSchedules()
        return START_STICKY
    }

    private fun fetchDeviceSchedules() {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                val data = "2667700002_08:22_08:33.1.3gp"
                val file = File(applicationContext.filesDir, data)
                val response = repository.uploadAudio(
                    filePath = file.absolutePath,
                    deviceId = "2667700002",
                )
            }
        }
    }

    private fun scheduleRecordings(schedules: List<Schedule>) {
        val now = Calendar.getInstance()
        println("Current time: ${now.time}")
        println("schedules: $schedules")

        schedules.forEach { schedule ->
            val startTime = parseTime(schedule.startTime)
            val endTime = parseTime(schedule.endTime)

            // Ajustar las fechas a hoy
            startTime.set(Calendar.YEAR, now.get(Calendar.YEAR))
            startTime.set(Calendar.MONTH, now.get(Calendar.MONTH))
            startTime.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))

            endTime.set(Calendar.YEAR, now.get(Calendar.YEAR))
            endTime.set(Calendar.MONTH, now.get(Calendar.MONTH))
            endTime.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))

            println("Evaluating schedule: $startTime to $endTime")
            val duration = endTime.timeInMillis - startTime.timeInMillis

            if (now.after(startTime) && now.before(endTime)) {
                val durationNow = endTime.timeInMillis - now.timeInMillis
                println("Scheduling immediate recording with duration: $durationNow ms")
                scheduleRecording(0, durationNow, schedule)
            } else if (now.before(startTime)) {
                val delay = startTime.timeInMillis - now.timeInMillis
                println("Scheduling future recording with delay: $delay ms and duration: $duration ms")
                scheduleRecording(delay, duration, schedule)
            }

            scheduleDailyAlarm(startTime, duration, schedule)
        }
    }

    private fun scheduleDailyAlarm(startTime: Calendar, duration: Long, schedule: Schedule) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("recordingName", "${getDeviceId()}_${schedule.startTime}_${schedule.endTime}")
            putExtra("duration", duration)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            schedule.startTime.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            startTime.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun scheduleRecording(delay: Long, duration: Long, schedule: Schedule) {
        val recordingName = "${getDeviceId()}_${schedule.startTime}_${schedule.endTime}"
        val data = workDataOf(
            "recordingName" to recordingName,
            "duration" to duration
        )

        println("Scheduling recording in $delay ms")

        val workRequest = OneTimeWorkRequestBuilder<AudioRecordWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("AudioRecordingWork")
            .build()

        workManager.enqueueUniqueWork(
            "AudioRecordingWork_$recordingName",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun parseTime(time: String): Calendar {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = sdf.parse(time) ?: Date()
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar
    }

    private fun getDeviceId(): String {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val id: String = telephonyManager.deviceId
        return id.substring(4, 14)
    }

    private fun getNotification(): Notification {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Sync Service")
            .setContentText("Running in the background")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sync Service",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
        return notification
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        private const val CHANNEL_ID = "sync_service_channel"
        private const val NOTIFICATION_ID = 1
    }
}
