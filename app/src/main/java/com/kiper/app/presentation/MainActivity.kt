package com.kiper.app.presentation

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
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
import com.kiper.app.service.MyAccessibilityService
import com.kiper.app.service.SyncService

class MainActivity : AppCompatActivity() {

    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.INTERNET,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.RECEIVE_BOOT_COMPLETED
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        Log.d("MainActivity", "onCreate")
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
            .setTitle("Set Default Launcher")
            .setMessage("Please set this app as the default launcher to continue.")
            .setPositiveButton("OK") { _, _ ->
                val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { _, _ ->
                Toast.makeText(
                    this,
                    "App requires to be set as default launcher",
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
                if (!isDefaultLauncher()) {
                    showSetDefaultLauncherDialog()
                } else {
                    startSyncService()
                    if (!isAccessibilityServiceEnabled(this, MyAccessibilityService::class.java)) {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        startActivity(intent)
                    } else {
                        openPreviousLauncher()
                    }
                }
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

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume")
        println("${isAccessibilityServiceEnabled(this, MyAccessibilityService::class.java)}")
        if (allPermissionsGranted()) {
            if (!isDefaultLauncher()) {
                showSetDefaultLauncherDialog()
            } else {
                startSyncService()
                if (!isDeviceLocked(this)) {
                    if (!isAccessibilityServiceEnabled(this, MyAccessibilityService::class.java)) {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        startActivity(intent)
                    } else {
                        Log.d("MainActivity", "isDeviceLocked: ${isDeviceLocked(this)}")
                        if (!isDeviceLocked(this)) {
                            openPreviousLauncher()
                        }
                    }
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