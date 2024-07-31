package com.kiper.core.framework.worker

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kiper.core.domain.model.isIntoSchedule
import com.kiper.core.framework.audioRecorder.AndroidAudioRecorder
import com.kiper.core.util.Constants.EVENT_TYPE_AUDIO
import com.kiper.core.util.generateFileName
import com.kiper.core.util.getRemainingDurationFromNameFile
import com.kiper.core.util.getScheduledFromNameFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioRecordWorker(
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


            startRecording(file.absolutePath)
            isRecording = true
            delay(if (eventType != EVENT_TYPE_AUDIO) remainingDuration else duration)
            stopRecording()
            isRecording = false
            Result.success()
        } catch (e: Exception) {
            Log.e("AudioRecordWorker", "Recording failed", e)
            stopRecording()
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


    private fun startRecording(filePath: String) {
        recorder.start(File(filePath))
        Log.i("AudioRecordWorker", "Started recording to $filePath")
    }

    private fun stopRecording() {
        recorder.stop()
        Log.i("AudioRecordWorker", "Stopped recording")
    }

}
