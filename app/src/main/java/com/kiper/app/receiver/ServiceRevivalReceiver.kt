package com.kiper.app.receiver

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kiper.app.service.SyncService

class ServiceRevivalReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("ServiceRevivalReceiver", "Evento recibido: $action")

        if (!isServiceRunning(context, SyncService::class.java)) {
            Log.d("ServiceRevivalReceiver", "Servicio no activo. Iniciando SyncService...")
            val serviceIntent = Intent(context, SyncService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }

    private fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}
