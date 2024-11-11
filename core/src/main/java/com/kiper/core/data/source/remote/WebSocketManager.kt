package com.kiper.core.data.source.remote
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.kiper.core.BuildConfig
import com.kiper.core.domain.model.WebSocketEventResponse
import com.kiper.core.domain.model.WebSocketMessageRequest
import com.kiper.core.domain.model.WebSocketSendRequest
import com.kiper.core.util.Constants.TYPE_SEND_CONNECTION
import com.kiper.core.util.Constants.TYPE_SEND_REGISTER
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketManager @Inject constructor() {

    private lateinit var client: OkHttpClient
    private lateinit var request: Request
    private var webSocket: WebSocket? = null
    private val reconnectHandler = Handler(Looper.getMainLooper())
    private val gson = Gson()
    private var retryDelay = 5000L
    private var deviceId: String = ""

    interface WebSocketCallback {
        fun onMessage(message: WebSocketEventResponse)
    }

    var callback: WebSocketCallback? = null

    fun start(deviceId: String) {
        this.deviceId = deviceId
        client = OkHttpClient.Builder()
            .pingInterval(30, TimeUnit.SECONDS) // Mantiene la conexión activa
            .retryOnConnectionFailure(true) // Reintenta automáticamente en fallos de conexión
            .build()

        request = Request.Builder().url(BuildConfig.WEB_SOCKET).build()
        connectWebSocket()
    }

    private fun stop() {
        webSocket?.close(1000, "Closing WebSocket")
        reconnectHandler.removeCallbacksAndMessages(null)
    }

    private fun connectWebSocket() {
        webSocket = client.newWebSocket(request, WebSocketEventListener())
    }

    fun hasActiveConnection(): Boolean {
        val isConnected = webSocket?.send(
            gson.toJson(
                WebSocketSendRequest(clientId = deviceId, type = TYPE_SEND_CONNECTION)
            )
        ) == true
        Log.i("WebSocketManager", "Has Active Connection: $isConnected")
        return isConnected
    }

    private fun send(message: String) {
        webSocket?.send(message)
    }

    private inner class WebSocketEventListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
            super.onOpen(webSocket, response)
            Log.d("WebSocket", "Connected")
            send(
                gson.toJson(WebSocketSendRequest(clientId = deviceId, type = TYPE_SEND_REGISTER))
            )
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            Log.d("WebSocket", "Message Received: $text")
            callback?.onMessage(parseWebSocketEvent(text))
            send(gson.toJson(WebSocketMessageRequest(clientId = deviceId)))
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            super.onMessage(webSocket, bytes)
            Log.d("WebSocket", "Received ByteString: $bytes")
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)
            Log.d("WebSocket", "Closing: $code / $reason")
            webSocket.close(1000, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            Log.d("WebSocket", "Closed: $code / $reason")
            scheduleReconnect()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
            super.onFailure(webSocket, t, response)
            Log.e("WebSocket", "Error: ${t.message}")
            scheduleReconnect()
        }
    }

    private fun parseWebSocketEvent(message: String): WebSocketEventResponse {
        return try {
            gson.fromJson(message, WebSocketEventResponse::class.java)
        } catch (e: Exception) {
            Log.e("WebSocketManager", "Failed to parse event: ${e.message}")
            WebSocketEventResponse()
        }
    }

    private fun scheduleReconnect() {
        Handler(Looper.getMainLooper()).postDelayed({
            Log.i("WebSocket", "Reconnecting with delay: $retryDelay ms")
            reconnect()
        }, retryDelay)
    }

    private fun reconnect() {
        stop()

        client = OkHttpClient.Builder()
            .pingInterval(60, TimeUnit.SECONDS)
            .build()

        start(deviceId = deviceId)
    }
}
