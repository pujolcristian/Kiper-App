package com.kiper.core.domain.usecase

import com.kiper.core.domain.model.CapsulesResponse
import com.kiper.core.domain.repository.CapsuleRepository
import kotlinx.coroutines.flow.Flow

class GetCapsuleMessageUseCase(private val capsuleRepository: CapsuleRepository) {
    suspend operator fun invoke(deviceId: String): Flow<CapsulesResponse?> {
        return capsuleRepository.getCapsuleMessage(deviceId)
    }
}
