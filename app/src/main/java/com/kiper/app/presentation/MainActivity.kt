package com.kiper.app.presentation

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.app.ActivityManager
import android.app.KeyguardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kiper.app.R
import com.kiper.app.receiver.ScreenStateReceiver
import com.kiper.app.service.SyncService

class MainActivity : AppCompatActivity() {

    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.INTERNET,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.RECEIVE_BOOT_COMPLETED,
        Manifest.permission.READ_PHONE_STATE)
    private lateinit var screenStateReceiver: ScreenStateReceiver


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        Log.d("MainActivity", "onCreate")
        screenStateReceiver = ScreenStateReceiver()
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF).apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(screenStateReceiver, filter)
    }

    private fun allPermissionsGranted() = permissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun isDefaultLauncher(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        val currentLauncher = resolveInfo?.activityInfo?.packageName
        return currentLauncher == packageName
    }

    private fun showSetDefaultLauncherDialog() {
        AlertDialog.Builder(this)
            .setTitle("KIPER")
            .setMessage("Por favor, establece esta Kiper como el lanzador por defecto para continuar.")
            .setPositiveButton("OK") { _, _ ->
                val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { _, _ ->
                Toast.makeText(
                    this,
                    "Kiper debe configurarse como lanzador predeterminado",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .show()
    }

    private fun startSyncService() {
        val serviceIntent = Intent(this, SyncService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }


    private fun openPreviousLauncher() {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            component = ComponentName("com.sgtc.launcher", "com.sgtc.launcher.Launcher")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startSyncService()
                if (!isDefaultLauncher()) {
                    showSetDefaultLauncherDialog()
                } else {
                    if (!isDeviceLocked(this)) {
                        println("isDeviceLocked1: ${isDeviceLocked(this)}")
                        openPreviousLauncher()
                    } else {
                        println("isDeviceLocked2: ${isDeviceLocked(this)}")
                        closePreviousLauncher(this.applicationContext)
                    }
                }
                /*   if (!isAccessibilityServiceEnabled(this, MyAccessibilityService::class.java)) {
                       val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                       startActivity(intent)
                   } else {

                 */
            } else {
                Toast.makeText(this, "Permisos necesarios no otorgados", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun isAccessibilityServiceEnabled(
        context: Context,
        service: Class<out AccessibilityService>,
    ): Boolean {
        val expectedComponentName = ComponentName(context, service).flattenToString()
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)
        while (colonSplitter.hasNext()) {
            val componentName = colonSplitter.next()
            if (componentName.equals(expectedComponentName, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    private fun isDeviceLocked(context: Context): Boolean {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return keyguardManager.isKeyguardLocked || !powerManager.isInteractive
    }

    private fun closePreviousLauncher(context: Context) {
        Log.i("MainActivity", "Cerrando launcher")
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        // Cierra la aplicación del otro launcher (si es posible en tu caso específico)
        activityManager.killBackgroundProcesses("com.sgtc.launcher")
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume")
        if (allPermissionsGranted()) {
            startSyncService()
            if (!isDefaultLauncher()) {
                showSetDefaultLauncherDialog()
            } else {
                Log.d("MainActivity", "isDeviceLocked: ${isDeviceLocked(this)}")
                if (!isDeviceLocked(this)) {
                    println("isDeviceLocked3: ${isDeviceLocked(this)}")
                    openPreviousLauncher()
                } else {
                    println("isDeviceLocked4: ${isDeviceLocked(this)}")
                    closePreviousLauncher(this.applicationContext)
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSIONS)
        }
    }


    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}