package com.kiper.app.network

import android.content.Context
import android.content.Intent
import android.util.Log
import com.kiper.app.service.SyncService
import com.kiper.core.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class DefaultConnectivityChecker(
    private val context: Context
) : ConnectivityChecker {

    interface ResultNetwork {
        fun onInternetAccessResult(isConnected: Boolean, event: String)
    }

    var listener : ResultNetwork? = null

    override fun checkInternetAccess(event: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val hasInternetAccess = try {
                val url = URL("http://clients3.google.com/generate_204")
                val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
                urlConnection.connectTimeout = 9000
                urlConnection.connect()
                val isConnected = urlConnection.responseCode == 204
                urlConnection.disconnect()
                isConnected
            } catch (e: Exception) {
                Log.e("NetworkMonitor", "Error checking Internet access", e)
                false
            }
            handleInternetAccessResult(hasInternetAccess, event)
        }
    }

    private fun handleInternetAccessResult(isConnected: Boolean, event: String) {
        if (isConnected && event == Constants.EVENT_TYPE_PROCESS_AUDIO) {
            listener?.onInternetAccessResult(isConnected, event)
            Log.i("NetworkMonitor", "Internet access available. Initiating upload.")
            sendSyncServiceIntent(event)
        } else {
            listener?.onInternetAccessResult(isConnected, event)
            sendSyncServiceIntent(Constants.EVENT_CLOSE_CONNECTION)
            Log.i("NetworkMonitor", "No Internet access.")
        }
    }

    private fun sendSyncServiceIntent(event: String) {
        val syncServiceIntent = Intent(context, SyncService::class.java).apply {
            putExtra("upload_event_type", event)
        }
        context.startService(syncServiceIntent)
    }


}
