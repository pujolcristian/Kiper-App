package com.kiper.core.domain.usecase

import com.kiper.core.domain.repository.UpdateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CheckAndDownloadVersionUseCase(
    private val updateRepository: UpdateRepository,
) {
    suspend operator fun invoke(): Flow<Boolean> = flow {
        val result = updateRepository.checkAndDownloadUpdate()
        emit(result)
    }
}