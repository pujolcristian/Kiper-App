package com.kiper.core.data.repository

import com.kiper.core.data.source.remote.CapsuleRemoteDataSource
import com.kiper.core.domain.repository.CapsuleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CapsuleRepositoryImpl @Inject constructor(
    private val capsuleRemoteDataSource: CapsuleRemoteDataSource
) : CapsuleRepository {

    override suspend fun getCapsuleMessage(deviceId: String): Flow<String?> {
        return capsuleRemoteDataSource.getCapsuleMessage(deviceId)
    }
}