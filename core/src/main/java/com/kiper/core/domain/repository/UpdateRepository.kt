package com.kiper.core.domain.repository

interface UpdateRepository {
    suspend fun checkAndDownloadUpdate(): Boolean
}