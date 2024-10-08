package com.kiper.app.network

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.kiper.core.util.Constants
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit


class NetworkMonitor(
    private val context: Context,
) {

    interface ResultNetwork {
        fun onInternetAccessResult(isConnected: Boolean, event: String)
    }

    var listener: ResultNetwork? = null

    private val disposables = CompositeDisposable()


    @SuppressLint("CheckResult")
    fun checkNetworkAndSendIntent() {
        val disposable = ReactiveNetwork.observeNetworkConnectivity(context)
            .throttleFirst(5, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { connectivity ->
                val state = connectivity.state()
                val name = connectivity.typeName()
                Log.i("NetworkMonitor", "Network state: $state, type: $name")

                when {
                    connectivity.available() && name == "WIFI" -> {
                        checkInternetAccess(Constants.EVENT_TYPE_PROCESS_AUDIO)
                    }

                    connectivity.available() && name == "MOBILE" -> {
                        checkInternetAccess(Constants.EVENT_TYPE_AUDIO)
                    }

                    else -> {
                        checkInternetAccess(Constants.EVENT_CLOSE_CONNECTION)
                    }
                }
            }
        disposables.add(disposable)
    }

    fun clear() {
        disposables.clear()
    }

    private fun checkInternetAccess(event: String) {
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
        } else {
            listener?.onInternetAccessResult(isConnected, event)
        }
    }
}
