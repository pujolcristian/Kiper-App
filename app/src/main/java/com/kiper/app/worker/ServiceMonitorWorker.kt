package com.kiper.app.worker

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.util.Log
import com.kiper.app.service.SyncService

class ServiceMonitorWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val isServiceActive = isServiceRunning(SyncService::class.java)
        
        if (!isServiceActive) {
            Log.d("ServiceMonitorWorker", "El servicio no está activo. Iniciando servicio...")
            val serviceIntent = Intent(applicationContext, SyncService::class.java)
            applicationContext.startService(serviceIntent)
        } else {
            Log.d("ServiceMonitorWorker", "El servicio ya está activo.")
        }
        
        return Result.success()
    }

    // Método para verificar si el servicio está en ejecución
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val activityManager = applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}
