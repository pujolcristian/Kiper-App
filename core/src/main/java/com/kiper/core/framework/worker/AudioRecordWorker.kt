package com.kiper.core.framework.worker

import android.content.Context
import android.media.MediaRecorder
import android.provider.Settings
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioRecordWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private var mediaRecorder: MediaRecorder? = null

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val duration = inputData.getLong("duration", 0L)

        if (duration <= 0L) {
            return@withContext Result.failure()
        }

        try {
            val fileName = generateFileName()
            startRecording(fileName)
            delay(duration)
            stopRecording()
            Result.success()
        } catch (e: Exception) {
            Log.e("AudioRecordWorker", "Recording failed", e)
            stopRecording()
            Result.retry()
        }
    }

    private fun generateFileName(): String {
        val deviceId = Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val date = dateFormat.format(Date())
        return "audio_${deviceId}_${date}.3gp"
    }

    private fun startRecording(fileName: String) {
        val file = File(applicationContext.filesDir, fileName)
        if (file.exists()) {
            file.delete()
        }

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }

        Log.i("AudioRecordWorker", "Started recording to ${file.absolutePath}")
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            try {
                stop()
                Log.i("AudioRecordWorker", "Recording stopped")
            } catch (e: IllegalStateException) {
                Log.e("AudioRecordWorker", "Stop called in an invalid state", e)
            }
            release()
        }
        mediaRecorder = null
    }
}
