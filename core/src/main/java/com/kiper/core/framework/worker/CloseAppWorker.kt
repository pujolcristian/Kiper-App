package com.kiper.core.framework.worker

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.kiper.core.util.Constants.ACTION_CLOSE_APP
import com.kiper.core.util.Constants.LAUNCHER_PACKAGE

class CloseAppWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.d("CloseAppWorker", "Solicitando cierre de la app a AccessibilityService")
        
        // Envía un broadcast para ejecutar `closeApp` en el servicio
        val intent = Intent(ACTION_CLOSE_APP).apply {
            putExtra("packageName", LAUNCHER_PACKAGE)
        }
        applicationContext.sendBroadcast(intent)

        return Result.success()
    }
}
