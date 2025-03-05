package com.kiper.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface CapsuleRepository {
    suspend fun getCapsuleMessage(deviceId: String): Flow<String?>
}