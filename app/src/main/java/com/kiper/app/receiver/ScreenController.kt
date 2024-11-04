package com.kiper.app.receiver

import android.app.KeyguardManager
import android.content.Context
import android.os.PowerManager
import android.os.PowerManager.WakeLock

class ScreenController(private val context: Context) {
    private var wakeLock: WakeLock? = null
    private var keyguardLock: KeyguardManager.KeyguardLock? = null

    fun turnScreenOn() {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        // Adquiere el wake lock con SCREEN_DIM_WAKE_LOCK en lugar de FULL_WAKE_LOCK
        wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
            "MyApp::ScreenOnWakeLock"
        )
        wakeLock?.acquire(1 * 60 * 1000L)

        // Desactiva temporalmente el Keyguard (bloqueo de pantalla)
        keyguardLock = keyguardManager.newKeyguardLock("MyApp::KeyguardLock")
        keyguardLock?.disableKeyguard()
    }

    fun releaseScreen() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        keyguardLock?.reenableKeyguard()
    }
}
