package com.kiper.app.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kiper.app.presentation.MainActivity

class MyDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        Log.i("DeviceAdminSample", "Admin enabled")
    }

    override fun onLockTaskModeEntering(context: Context, intent: Intent, pkg: String) {
        Log.i("DeviceAdminSample", "Entering lock task mode")
        // Relanzar la app si detectas que fue cerrada
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("MyDeviceAdminReceiver", "Recibido: ${intent.action}")

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Ejecutar acción cuando el dispositivo se reinicia
            val startIntent = Intent(context, MainActivity::class.java)
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(startIntent)
        }
    }

}
