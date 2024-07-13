package com.kiper.core.domain.usecase

import com.kiper.core.domain.repository.AudioRepository
import javax.inject.Inject

class UploadAudioUseCase @Inject constructor(
    private val repository: AudioRepository
) {
    suspend fun execute(filePaths: List<String>, deviceId: String, eventType: String): List<Boolean> {
        val results = mutableListOf<Boolean>()
        for (filePath in filePaths) {
            val result = repository.uploadAudio(
                filePath = filePath,
                deviceId = deviceId,
                eventType = eventType
            )
            results.add(result)
        }
        return results
    }
}
