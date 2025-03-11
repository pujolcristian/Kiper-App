package com.kiper.core.domain.repository

import com.kiper.core.domain.model.CapsulesResponse
import kotlinx.coroutines.flow.Flow

interface CapsuleRepository {
    suspend fun getCapsuleMessage(deviceId: String): Flow<CapsulesResponse?>
}