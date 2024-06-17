package com.kiper.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kiper.app.service.SyncService

class RestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "RESTART_APP") {
            val serviceIntent = Intent(context, SyncService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}