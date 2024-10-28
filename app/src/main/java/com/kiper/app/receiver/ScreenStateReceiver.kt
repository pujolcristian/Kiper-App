package com.kiper.app.receiver

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kiper.app.presentation.MainActivity

class ScreenStateReceiver(private val activity: MainActivity) : BroadcastReceiver() {

    @SuppressLint("WearRecents")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return

        Log.d("ScreenStateReceiver", "Recibido: ${intent.action}")

        when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> {
                Log.d("ScreenStateReceiver", "Screen off")
                closePreviousLauncher(context = activity)
            }
            Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                Log.d("ScreenStateReceiver", "Screen on")
                openPreviousLauncher(context = context)
            }
        }
    }

    private fun openPreviousLauncher(context: Context) {
        Log.d("ScreenStateReceiver", "Intentando abrir el launcher anterior")
        val intent = Intent(Intent.ACTION_MAIN).apply {
            component = ComponentName("com.sgtc.launcher", "com.sgtc.launcher.Launcher")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun closePreviousLauncher(context: Context) {
        Log.i("ScreenStateReceiver", "Cerrando launcher")
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.moveTaskToFront(activity.taskId, 0)
        activityManager.killBackgroundProcesses("com.sgtc.launcher.launcher")
        activityManager.killBackgroundProcesses("com.sgtc.launcher")
    }
}
