package com.kiper.core.domain.usecase

import com.kiper.core.domain.repository.AudioRepository

class DeleteAllRecordingsUseCase(private val audioRepository: AudioRepository) {
    suspend operator fun invoke() {
        audioRepository.deleteAllRecordings()
    }
}
