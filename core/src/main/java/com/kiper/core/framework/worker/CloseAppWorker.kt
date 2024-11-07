package com.kiper.core.framework.worker

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class CloseAppWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.d("CloseAppWorker", "Solicitando cierre de la app a AccessibilityService")
        
        // Envía un broadcast para ejecutar `closeApp` en el servicio
        val intent = Intent("com.kiper.app.CLOSE_APP_ACTION").apply {
            putExtra("packageName", "com.sgtc.launcher")
        }
        applicationContext.sendBroadcast(intent)

        return Result.success()
    }
}
