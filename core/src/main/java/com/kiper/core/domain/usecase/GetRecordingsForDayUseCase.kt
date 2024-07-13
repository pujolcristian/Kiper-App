package com.kiper.core.domain.usecase

import com.kiper.core.data.source.local.entity.AudioRecordingEntity
import com.kiper.core.domain.model.AudioRecording
import com.kiper.core.domain.repository.AudioRepository

class GetRecordingsForDayUseCase(private val audioRepository: AudioRepository) {
    suspend operator fun invoke(startOfDay: Long, endOfDay: Long): List<AudioRecording> {
        return audioRepository.getRecordingsForDay(startOfDay, endOfDay)
    }
}
