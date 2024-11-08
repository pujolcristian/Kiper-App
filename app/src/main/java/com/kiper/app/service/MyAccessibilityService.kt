package com.kiper.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kiper.app.receiver.ScreenController
import com.kiper.app.receiver.ScreenStateReceiver
import com.kiper.app.receiver.ServiceRevivalReceiver
import com.kiper.core.framework.worker.CloseAppWorker
import java.util.concurrent.TimeUnit

class MyAccessibilityService : AccessibilityService() {



    private val handler = Handler(Looper.getMainLooper())
    private lateinit var screenStateReceiver: ScreenStateReceiver
    private lateinit var serviceRevivalReceiver: ServiceRevivalReceiver
    private var closeAppRunnable: Runnable? = null
    private var lastInteractionTime = 0L

    private val tryForForceStopButton = 30
    private val tryForConfirmationButton = 2


    private val closeAppReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("closeAppReceiver", "intent: ${intent?.action}")
            if (intent?.action == "com.kiper.app.CLOSE_APP_ACTION") {
                val packageName = intent.getStringExtra("packageName")
                packageName?.let {
                    closeApp(it)
                }
            }
        }
    }

    override fun onServiceConnected() {
        registerReceiver(closeAppReceiver, IntentFilter("com.kiper.app.CLOSE_APP_ACTION"))
        setupAccessibilityService()
        registerScreenReceiver()
        scheduleAppClosureWithWorkManager(applicationContext)
        openAppInfo()
    }

    private fun setupAccessibilityService() {
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            packageNames = null
        }
    }

    private fun registerScreenReceiver() {
        screenStateReceiver = ScreenStateReceiver(service = this)
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(screenStateReceiver, filter)

        serviceRevivalReceiver = ServiceRevivalReceiver()
        registerReceiver(serviceRevivalReceiver, IntentFilter(
            Intent.ACTION_ALL_APPS
        ))
    }

    fun openAppInfo() {
        Log.d(
            "MyAccessibilityService",
            "try - Abriendo información de la app ${shouldSkipInteraction()}- ${
                isDeviceLocked(applicationContext)
            }"
        )
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:com.sprd.engineermode")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        processForceStopButton()
        lastInteractionTime = System.currentTimeMillis()
        goToHome()

    }

    private fun shouldSkipInteraction(): Boolean {
        return System.currentTimeMillis() - lastInteractionTime < 10 * 60 * 1000
    }

    private fun processForceStopButton() {
        val forceStopButton = findNodeWithRetries("com.android.settings:id/right_button", tryForForceStopButton)
        Log.d("MyAccessibilityService", "forceStopButton: $forceStopButton")
        performClickOnNode(forceStopButton)
        processConfirmationButton()
    }

    private fun processConfirmationButton() {
        val confirmButton = findNodeWithRetries("OK", tryForConfirmationButton, byText = true)
        Log.d("MyAccessibilityService", "confirmButton: $confirmButton")
        performClickOnNode(confirmButton)
    }

    private fun findNodeWithRetries(identifier: String, retries: Int, byText: Boolean = false): AccessibilityNodeInfo? {
        try {
            repeat(retries) {
                val node = if (byText) findNodeByText(identifier) else findNodeByViewId(identifier)
                if (node != null) return node
                Thread.sleep(1000)
            }
        } catch (e: InterruptedException) {
            Log.e("MyAccessibilityService", "Error en la espera")
        }
        return null
    }

    private fun isDeviceLocked(context: Context): Boolean {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return keyguardManager.isKeyguardLocked || !powerManager.isInteractive
    }


    private fun findNodeByText(text: String): AccessibilityNodeInfo? {
        return rootInActiveWindow?.findAccessibilityNodeInfosByText(text)?.firstOrNull()
    }

    private fun findNodeByViewId(viewId: String): AccessibilityNodeInfo? {
        return rootInActiveWindow?.findAccessibilityNodeInfosByViewId(viewId)?.firstOrNull()
    }

    private fun performClickOnNode(node: AccessibilityNodeInfo?) {
        node?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    private fun goToHome() {
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && event.packageName == "com.sgtc.launcher") {
           // scheduleAppClosure()
        }
        Log.d("MyAccessibilityService", "TYPE_WINDOW_STATE_CHANGED: ${event?.eventType}, ${event?.packageName}")
    }

//     fun scheduleAppClosure() {
//         Log.d("MyAccessibilityService", "Scheduling app closure")
//        closeAppRunnable?.let { handler.removeCallbacks(it) }
//        closeAppRunnable = object : Runnable {
//            override fun run() {
//                closeApp("com.sgtc.launcher")
//                handler.postDelayed(this, 1 * 60 * 1000)
//                Log.d("MyAccessibility", "Ciclo de cierre de app")
//            }
//        }
//        handler.postDelayed(closeAppRunnable!!, 1 * 30 * 1000)
//    }

    private fun scheduleAppClosureWithWorkManager(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val closeAppWorkRequest = PeriodicWorkRequestBuilder<CloseAppWorker>(
            1, TimeUnit.MINUTES // Configura el intervalo de repetición
        ).build()
        workManager.cancelAllWorkByTag(WORK_TAG)
        workManager.enqueueUniquePeriodicWork(
            WORK_TAG,
            ExistingPeriodicWorkPolicy.REPLACE,
            closeAppWorkRequest
        )
    }

    private fun turnOnScreen() {
        val screenController = ScreenController(this)
        screenController.turnScreenOn()
        openAppInfo()
        Handler(Looper.getMainLooper()).postDelayed({
            screenController.releaseScreen()
        },10 * 1000)
    }



    override fun onInterrupt() {
        Log.d("MyAccessibilityService", "Service Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenStateReceiver)
    }

    fun closeApp(packageName: String) {
        Log.d("MyAccessibilityService", "Cerrando app: $packageName, locked:${isDeviceLocked(applicationContext)}")
        if (!isDeviceLocked(applicationContext)) return
        performGlobalAction(GLOBAL_ACTION_RECENTS)
        handler.postDelayed({
            val rootNode = rootInActiveWindow ?: return@postDelayed
            val dismissNodes = findNodesByResourceId(rootNode, "com.android.systemui:id/dismiss_task")

            openAppInfo()
            /*dismissNodes.forEach { node ->
                val parent = node.parent
                if (parent != null) {
                    for (i in 0 until parent.childCount) {
                        val child = parent.getChild(i)
                        if (child != null && child.className == "android.widget.TextView" && child.text != "Kiper-App") {
                            Log.d("MyAccessibilityService", "Cerrando: ${child.text}")
                            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            return@forEach
                        }
                    }
                }
            }
             */

            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        }, 1000)
    }

    private fun findNodesByResourceId(root: AccessibilityNodeInfo, resourceId: String): List<AccessibilityNodeInfo> {
        val result = mutableListOf<AccessibilityNodeInfo>()
        if (root.viewIdResourceName == resourceId) result.add(root)
        for (i in 0 until root.childCount) {
            root.getChild(i)?.let { result.addAll(findNodesByResourceId(it, resourceId)) }
        }
        return result
    }

    companion object {
        const val WORK_TAG = "CloseAppWork"
    }
}
