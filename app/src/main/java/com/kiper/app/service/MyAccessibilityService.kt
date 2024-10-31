package com.kiper.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.kiper.app.receiver.ScreenStateReceiver

class MyAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var closeAppRunnable: Runnable? = null
    private lateinit var screenStateReceiver: ScreenStateReceiver

    private var lastTime = 0L
    private var tryForFirstButton = 30
    private var tryForSecondButton = 2

    override fun onServiceConnected() {
        Log.d("MyAccessibilityService", "Servicio conectado")

        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        info.packageNames = null // Monitoriza todos los paquetes
        serviceInfo = info
        openAppInfo()
        screenStateReceiver = ScreenStateReceiver(service = this)
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF).apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(screenStateReceiver, filter)
    }

    fun openAppInfo(sleepTime: Long = 1000) {
        Log.d("MyAccessibilityService", "${System.currentTimeMillis() - lastTime < 5 * 60 * 1000}")
        if ((System.currentTimeMillis() - lastTime < 5 * 60 * 1000) && lastTime != 0L) {
            return
        }
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:com.sprd.engineermode")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        this@MyAccessibilityService.startActivity(intent)
        Thread.sleep(sleepTime)
        printAllNodes(rootInActiveWindow)
        var forceStopButton = findNodeByViewId("com.android.settings:id/right_button")
        Log.d("MyAccessibilityService", "Botón: $forceStopButton")
        while (forceStopButton == null && tryForFirstButton  != 0) {
            forceStopButton = findNodeByViewId("com.android.settings:id/right_button")
            Thread.sleep(1000)
            tryForFirstButton = --tryForFirstButton
            Log.d("MyAccessibilityService", "Botón: $forceStopButton")
        }
        performClickOnNode(forceStopButton)
        printAllNodes(rootInActiveWindow)
        Thread.sleep(sleepTime)
        var confirmButton = findNodeByText("OK")
        while (confirmButton == null && tryForSecondButton != 0) {
            confirmButton = findNodeByText("OK")
            Log.d("MyAccessibilityService2", "Botón: $confirmButton, $tryForSecondButton")
            Thread.sleep(1000)
            tryForSecondButton = --tryForSecondButton
        }
        confirmButton?.let {
            performClickOnNode(it)
        } ?: goHome()
        Thread.sleep(sleepTime)
        lastTime = System.currentTimeMillis()
        goHome()
    }

    // SETUP CULDOWN FOR TRY BUTTONS AND RESET WITH GOHOME ACTION

    fun goHome() {
        tryForFirstButton = 30
        tryForSecondButton = 10
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    private fun printAllNodes(node: AccessibilityNodeInfo?) {
        if (node == null) return

        // Imprimir el ID de vista (si existe), clase, y texto
        val viewId = node.viewIdResourceName
        val className = node.className
        val text = node.text

        Log.d("AccessibilityService", "ID: $viewId, Clase: $className, Texto: $text")

        // Recorre los hijos del nodo actual
        for (i in 0 until node.childCount) {
            printAllNodes(node.getChild(i))
        }
   }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.packageName == "com.sgtc.launcher") {
                scheduleAppClosure()
            }
        }
        Log.d("MyAccessibilityService", "Evento: ${event?.eventType}, ${event?.packageName}")
    }

    private fun findNodeByText(text: String): AccessibilityNodeInfo? {
        val rootNode = rootInActiveWindow ?: return null
        val nodes = rootNode.findAccessibilityNodeInfosByText(text)
        return if (nodes.isNotEmpty()) nodes[0] else null
    }

    private fun findNodeByViewId(viewId: String): AccessibilityNodeInfo? {
        val rootNode = rootInActiveWindow ?: return null
        val nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId)
        return if (nodes.isNotEmpty()) nodes[0] else null
    }

    private fun performClickOnNode(node: AccessibilityNodeInfo?) {
        node?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
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
            openAppInfo()
          //  closeApp("com.sgtc.launcher")
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
