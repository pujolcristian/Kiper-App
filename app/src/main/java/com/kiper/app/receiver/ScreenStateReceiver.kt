package com.kiper.app.receiver

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.kiper.app.presentation.MainActivity
import com.kiper.app.service.MyAccessibilityService

class ScreenStateReceiver(
    private val activity: MainActivity? = null,
    private val service: MyAccessibilityService? = null,
) : BroadcastReceiver() {

    private val handler = Handler(Looper.getMainLooper())
    private var screenOffRunnable: Runnable? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return

        when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> {
                //startScreenOffTimer()
                // startMainActivityTimer(context)
                Log.d("ScreenStateReceiver", "Pantalla apagada")
            }

            Intent.ACTION_SCREEN_ON -> {
                Log.d("ScreenStateReceiver", "Pantalla encendida")
                service?.closeApp("com.sgtc.launcher")
            }
        }
    }

    private fun startScreenOffTimer() {
        cancelScreenOffTimer()
        Log.d("ScreenStateReceiver", "Iniciando temporizador de apagado de pantalla")
        screenOffRunnable = Runnable {
            service?.openAppInfo()
            Log.d("ScreenStateReceiver", "Pantalla encendida después de 1-- minutos")
        }
        handler.postDelayed(screenOffRunnable!!, 1 * 30 * 1000)
    }
    private fun startMainActivityTimer(context: Context) {
        cancelScreenOffTimer()
        Log.d("ScreenStateReceiver", "launchMainActivity")
        screenOffRunnable = Runnable {
            launchMainActivity(context)
            Log.d("ScreenStateReceiver", "launchMainActivity")
        }
        handler.postDelayed(screenOffRunnable!!, 20 * 60 * 1000)
    }

    private fun cancelScreenOffTimer() {
        screenOffRunnable?.let { handler.removeCallbacks(it) }
        screenOffRunnable = null
    }

    @SuppressLint("WearRecents")
    private fun launchMainActivity(context: Context) {
        Log.d("ScreenStateReceiver", "Ejecutando MainActivity después de 8 segundos")
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun openPreviousLauncher(context: Context) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activity?.taskId?.let { activityManager.moveTaskToFront(it, 0) }
    }

    private fun closePreviousLauncher(context: Context) {
        Log.i("ScreenStateReceiver", "Cerrando launcher")
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activity?.taskId?.let { activityManager.moveTaskToFront(it, 0) }
    }
}
