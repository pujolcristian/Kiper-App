package com.kiper.core.domain.usecase

import com.kiper.core.domain.model.ScheduleResponse
import com.kiper.core.domain.repository.AudioRepository
import kotlinx.coroutines.flow.Flow

class GetDeviceSchedulesUseCase(private val audioRepository: AudioRepository) {
    suspend operator fun invoke(deviceId: String): Flow<List<ScheduleResponse>?> {
        return audioRepository.getDeviceSchedules(deviceId)
    }
}
