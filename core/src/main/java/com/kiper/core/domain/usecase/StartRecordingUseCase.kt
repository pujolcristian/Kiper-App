package com.kiper.core.domain.usecase

import com.kiper.core.domain.repository.AudioRepository

class StartRecordingUseCase(private val audioRepository: AudioRepository) {
    operator fun invoke(fileName: String) {
        audioRepository.startRecording(fileName)
    }
}