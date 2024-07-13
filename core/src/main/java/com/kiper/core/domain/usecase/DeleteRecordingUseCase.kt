package com.kiper.core.domain.usecase

import com.kiper.core.domain.repository.AudioRepository

class DeleteRecordingUseCase(private val audioRepository: AudioRepository) {
    suspend operator fun invoke(fileName: String) {
        audioRepository.deleteRecordingUploaded(fileName = fileName)
    }
}
