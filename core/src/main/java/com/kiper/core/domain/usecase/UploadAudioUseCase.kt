package com.kiper.core.domain.usecase

import com.kiper.core.domain.repository.NetworkRepository

class UploadAudioUseCase(private val networkRepository: NetworkRepository) {
    operator fun invoke(filePath: String): Boolean {
        return networkRepository.uploadFile(filePath)
    }
}