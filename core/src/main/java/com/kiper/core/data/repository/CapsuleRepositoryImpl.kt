package com.kiper.core.data.repository

import com.kiper.core.data.mappers.toCapsulesResponse
import com.kiper.core.data.source.remote.CapsuleRemoteDataSource
import com.kiper.core.domain.model.CapsulesResponse
import com.kiper.core.domain.repository.CapsuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CapsuleRepositoryImpl @Inject constructor(
    private val capsuleRemoteDataSource: CapsuleRemoteDataSource
) : CapsuleRepository {

    override suspend fun getCapsuleMessage(deviceId: String): Flow<CapsulesResponse?> {
        val response = capsuleRemoteDataSource.getCapsuleMessage(deviceId)
        return response.map { value -> value?.toCapsulesResponse() }
    }
}