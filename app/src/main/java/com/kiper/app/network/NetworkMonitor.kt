package com.kiper.app.network

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.kiper.app.service.SyncService
import com.kiper.core.util.Constants
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class NetworkMonitor(private val context: Context) {

    @SuppressLint("CheckResult")
    fun checkNetworkAndSendIntent() {
        ReactiveNetwork.observeNetworkConnectivity(context)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { connectivity ->
                val state = connectivity.state()
                val name = connectivity.typeName()
                Log.i("NetworkMonitor", "Network state: $state, type: $name")

                if (connectivity.available() && name == "WIFI") {
                    Log.i("NetworkMonitor", "Wi-Fi connection detected. Checking Internet access.")
                    checkInternetAccess()
                }
            }
    }

    private fun checkInternetAccess() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("http://clients3.google.com/generate_204")
                val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
                urlConnection.connectTimeout = 3000
                urlConnection.connect()
                if (urlConnection.responseCode == 204) {
                    Log.i("NetworkMonitor", "Internet access available. Initiating upload.")
                    sendSyncServiceIntent()
                } else {
                    Log.i("NetworkMonitor", "No Internet access. Response code: ${urlConnection.responseCode}")
                }
                urlConnection.disconnect()
            } catch (e: Exception) {
                Log.e("NetworkMonitor", "Error checking Internet access", e)
            }
        }
    }

    private fun sendSyncServiceIntent() {
        val syncServiceIntent = Intent(context, SyncService::class.java)
        syncServiceIntent.putExtra("upload_event_type", Constants.EVENT_TYPE_PROCESS_AUDIO)
        context.startService(syncServiceIntent)
    }
}
