package com.kiper.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kiper.app.presentation.MainActivity

class ScreenStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && (intent.action == Intent.ACTION_SCREEN_OFF)) {
            Log.d("ScreenStateReceiver", "Screen off")
            val serviceIntent = Intent(context, MainActivity::class.java)
            context?.startActivity(serviceIntent)
        }

        if (intent != null && intent.action == Intent.ACTION_SCREEN_ON || intent?.action == Intent.ACTION_USER_PRESENT) {
            Log.d("ScreenStateReceiver", "Screen on")
            val serviceIntent = Intent(context, MainActivity::class.java)
            context?.startActivity(serviceIntent)
        }
    }
}
