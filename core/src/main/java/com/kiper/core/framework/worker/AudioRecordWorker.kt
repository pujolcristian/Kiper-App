package com.kiper.core.framework.worker

import android.content.Context
import android.media.MediaRecorder
import android.provider.Settings
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kiper.core.data.Schedule
import com.kiper.core.util.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AudioRecordWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private var mediaRecorder: MediaRecorder? = null

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val duration = inputData.getLong("duration", 0L)
        val recordingName = inputData.getString("recordingName")

        if (duration <= 0L) {
            return@withContext Result.failure()
        }

        try {
            val directory = applicationContext.filesDir

            val fileName = getUniqueFileName(directory, recordingName ?: generateFileName())

            val file = File(directory, fileName)

            val divFileName = file.name.split("_")
            if (!scheduleRecordings(Schedule(divFileName.getOrNull(1) ?: "00:00", divFileName.getOrNull(2) ?: "00:00"))) {
                return@withContext Result.failure()
            }

            startRecording(file.absolutePath)
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

    private fun getUniqueFileName(directory: File, baseName: String): String {
        var file = File(directory, "$baseName.3gp")
        var index = 1

        while (file.exists()) {
            file = File(directory, "$baseName.$index.3gp")
            index++
        }

        return file.name
    }

    private fun startRecording(filePath: String) {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(filePath)
            prepare()
            start()
        }

        Log.i("AudioRecordWorker", "Started recording to $filePath")
    }

    private fun scheduleRecordings(schedule: Schedule): Boolean {
        val now = Calendar.getInstance()

        val startTime = Util().parseTime(schedule.startTime)
        val endTime = Util().parseTime(schedule.endTime)

        // Ajustamos las fechas al día de hoy
        val today = Calendar.getInstance()
        startTime.set(Calendar.YEAR, today.get(Calendar.YEAR))
        startTime.set(Calendar.MONTH, today.get(Calendar.MONTH))
        startTime.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH))

        endTime.set(Calendar.YEAR, today.get(Calendar.YEAR))
        endTime.set(Calendar.MONTH, today.get(Calendar.MONTH))
        endTime.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH))

        println("Evaluating schedule: ${schedule.startTime} to ${schedule.endTime}")
        println("Current time: ${now.time}")
        println("Start time: ${ now <= endTime} && ${now >= startTime}")

        return now <= endTime && now >= startTime
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