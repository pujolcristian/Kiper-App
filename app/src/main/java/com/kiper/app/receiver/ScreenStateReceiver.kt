package com.kiper.app.receiver

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kiper.app.presentation.MainActivity
import com.kiper.app.service.MyAccessibilityService

class ScreenStateReceiver(
    private val activity: MainActivity? = null,
    private val service: MyAccessibilityService? = null,
) : BroadcastReceiver() {

    @SuppressLint("WearRecents")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return


        when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> {
               // service?.closeApp("com.sgtc.launcher")
                service?.openAppInfo()
                if (activity != null) {
                    //closePreviousLauncher(context = activity)
                }
            }

            Intent.ACTION_SCREEN_ON -> {
                Log.d("ScreenStateReceiver", "Screen on")
                service?.goHome()
                //openPreviousLauncher(context = context)
            }
        }
    }

    private fun openPreviousLauncher(context: Context) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activity?.taskId?.let { activityManager.moveTaskToFront(it, 0) }
    }

    private fun closePreviousLauncher(context: Context) {
        Log.i("ScreenStateReceiver", "Cerrando launcher")
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activity?.taskId?.let { activityManager.moveTaskToFront(it, 0) }
        activityManager.killBackgroundProcesses("com.sgtc.launcher.launcher")
        activityManager.killBackgroundProcesses("com.sgtc.launcher")
    }
}