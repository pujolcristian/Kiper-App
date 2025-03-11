package com.kiper.app.presentation

import android.Manifest
import android.app.Dialog
import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.kiper.app.R
import com.kiper.app.presentation.adapters.CardAdapter
import com.kiper.app.receiver.MyDeviceAdminReceiver
import com.kiper.app.receiver.ScreenStateReceiver
import com.kiper.app.service.SyncService
import com.kiper.core.util.Constants.ACTION_SHOW_CUSTOM_DIALOG
import com.kiper.core.util.Constants.EXTRA_MESSAGE
import com.kiper.core.util.Constants.EXTRA_SHOW_DIALOG
import com.kiper.core.util.Constants.LAUNCHER_ACTIVITY
import com.kiper.core.util.Constants.LAUNCHER_PACKAGE
import com.kiper.core.util.Constants.REQUEST_CODE_ENABLE_ADMIN
import com.kiper.core.util.Constants.REQUEST_CODE_PERMISSIONS
import com.kiper.core.util.Constants.REQUEST_UNKNOWN_SOURCES_PERMISSION_CODE
import com.kiper.core.util.Constants.TAG_MAIN_ACTIVITY

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.INTERNET,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.RECEIVE_BOOT_COMPLETED,
        Manifest.permission.READ_PHONE_STATE
    )
    private lateinit var screenStateReceiver: ScreenStateReceiver

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponent: ComponentName
    private lateinit var adapter: CardAdapter

    private var isShowingDialog = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        Log.d(TAG_MAIN_ACTIVITY, "onCreate")

        // Register the broadcast receiver
        val registerFilter = IntentFilter(ACTION_SHOW_CUSTOM_DIALOG)
        registerReceiver(webSocketReceiver, registerFilter)

        if (intent?.getBooleanExtra(EXTRA_SHOW_DIALOG, false) == true) {
            val messages: List<String>? = intent.getStringArrayListExtra(EXTRA_MESSAGE)
            showCustomDialog(messages = messages)
            isShowingDialog = true
        }

        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, MyDeviceAdminReceiver::class.java)

        if (!devicePolicyManager.isAdminActive(adminComponent)) {
            requestDeviceAdminPermission()
        } else {
            screenStateReceiver = ScreenStateReceiver()
            val filter = IntentFilter(Intent.ACTION_SCREEN_OFF).apply {
                addAction(Intent.ACTION_USER_PRESENT)
                addAction(Intent.ACTION_SCREEN_ON)
            }
            registerReceiver(screenStateReceiver, filter)
        }
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
            .setTitle(getString(R.string.kiper))
            .setMessage(getString(R.string.please_make_kiper_as_a_default_launcher))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                Toast.makeText(
                    this,
                    getString(R.string.kiper_must_be_default_launcher),
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
            component = ComponentName(LAUNCHER_PACKAGE, LAUNCHER_ACTIVITY)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    getString(R.string.required_permissions_not_granted), Toast.LENGTH_SHORT
                ).show()
                finish()
            }

            startSyncService()

            if (!isDefaultLauncher()) {
                showSetDefaultLauncherDialog()
                return
            }

            if (!devicePolicyManager.isAdminActive(adminComponent)) {
                requestDeviceAdminPermission()
                return
            }

            if (!packageManager.canRequestPackageInstalls()) {
                requestUnknownSourcesPermission()
                return
            }

            if (!Settings.canDrawOverlays(this)) {
                requestOverlayPermission()
                return
            }
            if (isShowingDialog) return
            openPreviousLauncher()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ENABLE_ADMIN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(
                    this,
                    getString(R.string.device_administrator_enabled), Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.device_administrator_not_enabled),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG_MAIN_ACTIVITY, "onResume")

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSIONS)
            return
        }

        startSyncService()

        if (!isDefaultLauncher()) {
            showSetDefaultLauncherDialog()
            return
        }

        if (!devicePolicyManager.isAdminActive(adminComponent)) {
            requestDeviceAdminPermission()
            return
        }

        registerScreenStateReceiver()

        if (!packageManager.canRequestPackageInstalls()) {
            requestUnknownSourcesPermission()
            return
        }

        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
            return
        }
        if (isShowingDialog) return
        openPreviousLauncher()
    }

    private fun registerScreenStateReceiver() {
        screenStateReceiver = ScreenStateReceiver()
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF).apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(screenStateReceiver, filter)
    }

    private fun requestUnknownSourcesPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
            data = Uri.parse("package:$packageName")
        }
        startActivityForResult(intent, REQUEST_UNKNOWN_SOURCES_PERMISSION_CODE)
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, 1234)
    }


    private fun requestDeviceAdminPermission() {
        val componentName = ComponentName(this, MyDeviceAdminReceiver::class.java)
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                .putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    getString(R.string.device_admin_explanation)
                )
        }
        startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
    }

    private val webSocketReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("MainActivity", "onReceive: ${intent?.action}")
            if (intent?.action == ACTION_SHOW_CUSTOM_DIALOG) {
                // Bring the app to foreground
                val activityIntent = Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putStringArrayListExtra(EXTRA_MESSAGE, intent.getStringArrayListExtra(EXTRA_MESSAGE))
                    putExtra(EXTRA_SHOW_DIALOG, true)  // Pass flag to show dialog
                }
                context?.startActivity(activityIntent)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(webSocketReceiver)
    }

    private fun showCustomDialog(messages: List<String>?) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.care_dialog)
        dialog.setCancelable(true)

        val viewPager = dialog.findViewById<ViewPager2>(R.id.viewPager)

        adapter = CardAdapter(messages ?: emptyList())
        viewPager.adapter = adapter

        val btnClose = dialog.findViewById<Button>(R.id.btnClose)
        btnClose.setOnClickListener {
            dialog.dismiss()
            isShowingDialog = false
            openPreviousLauncher()
        }

        dialog.show()

        // Customize window settings
        val window = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

}
