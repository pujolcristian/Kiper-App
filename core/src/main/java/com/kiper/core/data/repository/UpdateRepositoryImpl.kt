package com.kiper.core.data.repository

import com.kiper.core.data.source.remote.UpdateAppRemoteDataSource
import com.kiper.core.domain.repository.UpdateRepository

class UpdateRepositoryImpl(
    private val updateAppRemoteDataSource: UpdateAppRemoteDataSource,
) : UpdateRepository {
    override suspend fun checkAndDownloadUpdate(): Boolean {
        return updateAppRemoteDataSource.checkAndDownloadUpdate()
    }

}