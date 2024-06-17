package com.kiper.core.domain.repository

interface NetworkRepository {
    fun isNetworkAvailable(): Boolean
    fun uploadFile(filePath: String): Boolean
}