package com.kiper.core.data.repository

import android.content.Context
import com.kiper.core.domain.repository.NetworkRepository
import com.kiper.core.util.NetworkUtils

class NetworkRepositoryImpl(private val context: Context) : NetworkRepository {

    override fun isNetworkAvailable(): Boolean {
        return NetworkUtils.isNetworkAvailable(context)
    }

    override fun uploadFile(filePath: String): Boolean {
        // Implement the file upload logic here
        return true
    }
}