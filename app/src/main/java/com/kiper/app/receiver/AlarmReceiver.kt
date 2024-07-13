package com.kiper.app.receiver
/*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.kiper.core.framework.worker.AudioRecordWorker

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val recordingName = intent.getStringExtra("recordingName") ?: return
        val duration = intent.getLongExtra("duration", 0L)

        val data = workDataOf(
            "recordingName" to recordingName,
            "duration" to duration
        )

        val workRequest = OneTimeWorkRequestBuilder<AudioRecordWorker>()
            .setInputData(data)
            .addTag("AudioRecordingWork") // Agregar la etiqueta para cancelación futura
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "AudioRecordingWork_$recordingName", // Nombre único por grabación
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
 */