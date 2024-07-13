package com.kiper.app.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kiper.core.framework.worker.UploadAudioWorker

class ConnectivityReceiver : BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        if (isConnected(context)) {
            val workRequest = OneTimeWorkRequestBuilder<UploadAudioWorker>()
                .build()
            println("Enqueueing UploadAudioWork")
            WorkManager.getInstance(context).enqueueUniqueWork(
                "UploadAudioWork",
                ExistingWorkPolicy.KEEP,
                workRequest
            )
        }
    }

    private fun isConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}
