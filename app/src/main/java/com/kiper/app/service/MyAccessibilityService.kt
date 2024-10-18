package com.kiper.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.kiper.app.receiver.ScreenStateReceiver

class MyAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var closeAppRunnable: Runnable? = null
    private lateinit var screenStateReceiver: ScreenStateReceiver

    override fun onServiceConnected() {
        Log.d("MyAccessibilityService", "Servicio conectado")

        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        info.packageNames = null // Monitoriza todos los paquetes
        serviceInfo = info

        screenStateReceiver = ScreenStateReceiver(this)
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF).apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(screenStateReceiver, filter)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.packageName == "com.sgtc.launcher") {
                scheduleAppClosure()
            }
        }
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenStateReceiver)
    }

    private fun scheduleAppClosure() {
        if (closeAppRunnable != null) {
            handler.removeCallbacks(closeAppRunnable!!)
        }
        closeAppRunnable = Runnable {
            closeApp("com.sgtc.launcher")
        }
        handler.postDelayed(closeAppRunnable!!, 20 * 60 * 1000)
    }

    fun closeApp(packageName: String) {
        Log.d("MyAccessibilityService", "Cerrando app: $packageName")
        performGlobalAction(GLOBAL_ACTION_RECENTS)
        handler.postDelayed({
            val rootNode = rootInActiveWindow ?: return@postDelayed
            val dismissNodes = findNodesByResourceId(rootNode, "com.android.systemui:id/dismiss_task")

            dismissNodes.forEach { node ->
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
            performGlobalAction(GLOBAL_ACTION_HOME)
        }, 1000)
    }

    private fun findNodesByResourceId(root: AccessibilityNodeInfo, resourceId: String): List<AccessibilityNodeInfo> {
        val result = mutableListOf<AccessibilityNodeInfo>()
        if (root.viewIdResourceName != null && root.viewIdResourceName == resourceId) {
            result.add(root)
        }
        for (i in 0 until root.childCount) {
            val child = root.getChild(i)
            if (child != null) {
                result.addAll(findNodesByResourceId(child, resourceId))
            }
        }
        return result
    }
}
