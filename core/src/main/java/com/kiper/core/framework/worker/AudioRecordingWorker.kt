package com.kiper.core.framework.worker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kiper.core.domain.model.isIntoSchedule
import com.kiper.core.framework.audioRecorder.AndroidAudioRecorder
import com.kiper.core.framework.service.AudioRecordingService
import com.kiper.core.util.Constants.EVENT_TYPE_AUDIO
import com.kiper.core.util.generateFileName
import com.kiper.core.util.getRemainingDurationFromNameFile
import com.kiper.core.util.getScheduledFromNameFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioRecordingWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    private val recorder by lazy {
        AndroidAudioRecorder(applicationContext)
    }
    private var isRecording: Boolean = false
    override suspend fun doWork(): Result = withContext(Dispatchers.Main) {
        val duration = inputData.getLong("duration", 0L)
        val recordingName = inputData.getString("recordingName")
        val eventType = inputData.getString("eventType")

        val remainingDuration = recordingName?.getRemainingDurationFromNameFile() ?: 0L

        if (duration <= 0L) {
            return@withContext Result.failure()
        }

        if (remainingDuration <= 0L && eventType != EVENT_TYPE_AUDIO) {
            return@withContext Result.failure()
        }

        try {
            val directory = applicationContext.filesDir
            val name = recordingName?.generateFileName() ?: generateFileName()
            val file = File(directory, name)
            if (!file.name.getScheduledFromNameFile().isIntoSchedule()
                && eventType != EVENT_TYPE_AUDIO
            ) {
                return@withContext Result.failure()
            }
            if (isRecording) {
                return@withContext Result.failure()
            }

            if (eventType == EVENT_TYPE_AUDIO) {
                val interruptIntent =
                    Intent(applicationContext, AudioRecordingService::class.java).apply {
                        action = AudioRecordingService.ACTION_RECORD_30_SECONDS
                        putExtra("recordingName", name)
                        putExtra("remainingDuration", duration)
                    }

                applicationContext.startService(interruptIntent)
            } else {
                // Start the AudioRecordingService
                val intent = Intent(applicationContext, AudioRecordingService::class.java).apply {
                    action = AudioRecordingService.ACTION_START_RECORDING
                    putExtra("recordingName", name)
                    putExtra("remainingDuration", duration)
                }

                applicationContext.startForegroundService(intent)

            }

            Result.success()
        } catch (e: Exception) {
            Log.e("AudioRecordWorker", "Recording failed", e)
            isRecording = false
            Result.retry()
        }
    }

    @SuppressLint("HardwareIds")
    private fun generateFileName(): String {
        val deviceId = Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val date = dateFormat.format(Date())
        return "audio_${deviceId}_${date}.3gp"
    }

}