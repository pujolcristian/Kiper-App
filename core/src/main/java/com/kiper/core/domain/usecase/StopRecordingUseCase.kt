package com.kiper.core.domain.usecase

import com.kiper.core.domain.entity.AudioFile
import com.kiper.core.domain.repository.AudioRepository

class StopRecordingUseCase(private val audioRepository: AudioRepository) {
    operator fun invoke(): AudioFile {
        return audioRepository.stopRecording()
    }
}