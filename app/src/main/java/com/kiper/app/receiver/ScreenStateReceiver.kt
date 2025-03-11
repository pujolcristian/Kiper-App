package com.kiper.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kiper.app.service.MyAccessibilityService
import com.kiper.core.util.Constants.LAUNCHER_PACKAGE

class ScreenStateReceiver(
    private val service: MyAccessibilityService? = null,
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return

        when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> {
                Log.d("ScreenStateReceiver", "Pantalla apagada")
            }

            Intent.ACTION_SCREEN_ON -> {
                Log.d("ScreenStateReceiver", "Pantalla encendida")
                service?.closeApp(LAUNCHER_PACKAGE)
            }
        }
    }
}
