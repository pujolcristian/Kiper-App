package com.kiper.app.battery

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings

object BatteryOptimizationHelper {

    private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun requestIgnoreBatteryOptimizations(context: Context) {
        if (!isIgnoringBatteryOptimizations(context)) {
            val intent = Intent().apply {
                action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
            }
            context.startActivity(intent)
        }
    }
}