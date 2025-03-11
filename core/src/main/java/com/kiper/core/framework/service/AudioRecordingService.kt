package com.kiper.core.framework.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.kiper.core.framework.audioRecorder.AndroidAudioRecorder
import com.kiper.core.util.Constants.TAG_RECORDING_SERVICE
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class AudioRecordingService : Service() {

    @Inject
    lateinit var recorder: AndroidAudioRecorder

    private var isRecording: Boolean = false
    private var remainingDuration: Long = 0L
    private var recordingName: String? = null
    private var thirtySecondsRecordingName: String? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Ensure startForeground is called immediately
        if (!isRecording) {
            startForeground(NOTIFICATION_ID, createNotification())
        }

        when (intent?.action) {
            ACTION_START_RECORDING -> {
                recordingName = intent.getStringExtra("recordingName")
                remainingDuration = intent.getLongExtra("remainingDuration", 0L)
                startRecording()
            }

            ACTION_STOP_RECORDING -> stopRecording()
            ACTION_PAUSE_RECORDING -> pauseRecording()
            ACTION_RECORD_30_SECONDS -> {
                thirtySecondsRecordingName = intent.getStringExtra("recordingName")
                recordThirtySeconds()
            }
        }
        return START_STICKY
    }

    private fun createNotification(): Notification {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Recording Audio")
            .setContentText("Recording in progress...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID, "Audio Recording Service", NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
        return notification
    }

    private fun startRecording() {
        if (isRecording || recordingName.isNullOrEmpty() || remainingDuration <= 0) {
            Log.w(TAG_RECORDING_SERVICE, "Cannot start recording: already recording or invalid parameters")
            return
        }

        try {
            val directory = applicationContext.filesDir
            val file = File(directory, recordingName!!)
            recorder.start(file)
            isRecording = true
            Log.d(TAG_RECORDING_SERVICE, "Recording started for file: $file")

            // Schedule stop after remainingDuration
            Handler(Looper.getMainLooper()).postDelayed({
                if (isRecording) stopRecording()
            }, remainingDuration)

        } catch (e: Exception) {
            Log.e(TAG_RECORDING_SERVICE, "Failed to start recording", e)
            isRecording = false
        }
    }

    private fun stopRecording() {
        if (!isRecording) {
            Log.w(TAG_RECORDING_SERVICE, "Cannot stop recording: no recording in progress")
            return
        }

        try {
            recorder.stop()
            isRecording = false
            Log.d(TAG_RECORDING_SERVICE, "Recording stopped successfully")
        } catch (e: Exception) {
            Log.e(TAG_RECORDING_SERVICE, "Failed to stop recording", e)
        } finally {
            stopForeground(true)
            stopSelf()
        }
    }


    private fun pauseRecording() {
        Log.d(TAG_RECORDING_SERVICE, "Pausing recording $isRecording")
        Log.d(TAG_RECORDING_SERVICE, "Recording paused")
        recorder.stop()
        isRecording = false
    }

    private fun recordThirtySeconds() {
        if (thirtySecondsRecordingName.isNullOrEmpty()) {
            Log.w(
                TAG_RECORDING_SERVICE,
                "Cannot start 30-second recording: no active recording or invalid parameters"
            )
            return
        }

        pauseRecording()

        Handler(Looper.getMainLooper()).postDelayed({
            try {
                val directory = applicationContext.filesDir
                val file = File(directory, thirtySecondsRecordingName!!)
                recorder.start(file)
                isRecording = true

                Log.d(TAG_RECORDING_SERVICE, "Started 30-second recording to file: $file")

                // Schedule stop after 30 seconds
                Handler(Looper.getMainLooper()).postDelayed({
                    stopRecording()

                    // Resume the main recording
                    startRecording()
                }, 30_000)
            } catch (e: Exception) {
                Log.e(TAG_RECORDING_SERVICE, "Failed to record 30 seconds", e)
                startRecording() // Attempt to resume main recording
            }
        }, 6_000)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START_RECORDING = "com.kiper.app.ACTION_START_RECORDING"
        const val ACTION_STOP_RECORDING = "com.kiper.app.ACTION_STOP_RECORDING"
        const val ACTION_PAUSE_RECORDING = "com.kiper.app.ACTION_PAUSE_RECORDING"
        private const val CHANNEL_ID = "AudioRecordingServiceChannel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_RECORD_30_SECONDS = "com.kiper.app.ACTION_RECORD_30_SECONDS"
    }
}
