package com.kiper.core.data.source.remote

import com.kiper.core.data.ApiService
import com.kiper.core.data.dto.CapsulesResponseDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CapsuleRemoteDataSource @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getCapsuleMessage(deviceId: String): Flow<CapsulesResponseDto?> {
        val response = apiService.getCapsuleMessage(deviceId)

        return if (response.isSuccessful) {
            flow { emit(response.body()?.data) }
        } else {
            flow { emit(null) }
        }
    }

}