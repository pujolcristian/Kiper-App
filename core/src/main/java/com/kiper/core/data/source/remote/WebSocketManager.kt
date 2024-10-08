package com.kiper.core.data.source.remote

import com.google.gson.Gson
import com.kiper.core.BuildConfig
import com.kiper.core.domain.model.WebSocketEventResponse
import com.kiper.core.domain.model.WebSocketMessageRequest
import com.kiper.core.domain.model.WebSocketSendRequest
import com.kiper.core.util.Constants.TYPE_SEND_REGISTER
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketManager @Inject constructor() {

    private lateinit var client: OkHttpClient
    private lateinit var request: Request
    private var webSocket: WebSocket? = null
    private var deviceId: String = ""

    interface WebSocketCallback {
        fun onMessage(message: WebSocketEventResponse)
    }

    var callback: WebSocketCallback? = null

    fun start(deviceId: String) {
        this.deviceId = deviceId
        client = OkHttpClient()
        request = Request.Builder().url(BuildConfig.WEB_SOCKET).build()
        webSocket = client.newWebSocket(request, WebSocketEventListener())
        client.dispatcher.executorService.shutdown()
    }

    fun stop() {
        webSocket?.cancel()
    }

    private fun send(message: String) {
        webSocket?.send(message)
    }


    fun hasActiveConnection(): Boolean {
        return webSocket != null && webSocket?.send("ping") == true
    }

    private inner class WebSocketEventListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
            super.onOpen(webSocket, response)
            println("WebSocket Opened")
            send(
                Gson().toJson(
                    WebSocketSendRequest(
                        clientId = deviceId, type = TYPE_SEND_REGISTER
                    )
                )
            )
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            println("WebSocket Received Message: $text")
            callback?.onMessage(getWebSocketEventFromString(text))

            send(
                Gson().toJson(
                    WebSocketMessageRequest(
                        clientId = deviceId
                    )
                )
            )

        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            super.onMessage(webSocket, bytes)
            println("WebSocket Received ByteString: $bytes")
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)
            webSocket.close(1000, null)
            println("WebSocket Closing: $code / $reason")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            reconnect()
            println("WebSocket Closed: $code / $reason")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
            super.onFailure(webSocket, t, response)
            println("WebSocket Error: ${t.message}")
            reconnect()
        }
    }

    private fun getWebSocketEventFromString(message: String): WebSocketEventResponse {
        return try {
            Gson().fromJson(message, WebSocketEventResponse::class.java)
        } catch (e: Exception) {
            WebSocketEventResponse()
        }
    }

    private fun reconnect() {
        Thread.sleep(5000)
        stop()
        start(deviceId = deviceId)
    }

}
