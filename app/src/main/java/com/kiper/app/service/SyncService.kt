package com.kiper.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.kiper.core.framework.worker.AudioRecordWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class SyncService : Service() {

    @Inject
    lateinit var workManager: WorkManager

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, getNotification())
        scheduleRecordings()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scheduleRecordings()
        return START_STICKY
    }

    private fun scheduleRecordings() {
        val now = Calendar.getInstance()
        val recordingSchedules = listOf(
            Pair(7, 0),  // 7:00 AM to 8:00 AM
            Pair(9, 0), // 1:50 PM to 2:50 PM
            Pair(15, 0)  // 3:00 PM to 4:00 PM
        )

        recordingSchedules.forEach { schedule ->
            val startTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, schedule.first)
                set(Calendar.MINUTE, schedule.second)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val endTime = startTime.clone() as Calendar
            endTime.add(Calendar.HOUR_OF_DAY, 1)

            if (now.before(endTime) && now.after(startTime)) {
                val delay = startTime.timeInMillis - now.timeInMillis
                if (delay > 0) {
                    logSchedule("Scheduled to start in $delay ms", startTime, endTime)
                    scheduleWork(delay, TimeUnit.MILLISECONDS.toMillis(3600000)) // 1 hora
                } else if (now.after(startTime) && now.before(endTime)) {
                    val remainingTime = endTime.timeInMillis - now.timeInMillis
                    logSchedule("Scheduled to start immediately for remaining $remainingTime ms", startTime, endTime)
                    scheduleWork(0, remainingTime)
                }
            }
        }
    }

    private fun scheduleWork(delay: Long, duration: Long) {
        val recordingName = "recording_${System.currentTimeMillis()}"
        val data = workDataOf(
            "recordingName" to recordingName,
            "duration" to duration
        )

        val workRequest = OneTimeWorkRequestBuilder<AudioRecordWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        workManager.enqueueUniqueWork(
            "AudioRecordingWork",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun logSchedule(message: String, startTime: Calendar, endTime: Calendar) {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        val startStr = dateFormat.format(startTime.time)
        val endStr = dateFormat.format(endTime.time)
        println("$message (Start: $startStr, End: $endStr)")
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
        restartService()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun restartService() {
        val restartIntent = Intent(applicationContext, SyncService::class.java)
        startService(restartIntent)
    }

    companion object {
        private const val CHANNEL_ID = "sync_service_channel"
        private const val NOTIFICATION_ID = 1
    }
}
