package com.kiper.core.domain.usecase

import android.util.Log
import com.kiper.core.domain.repository.AudioRepository
import javax.inject.Inject

class UploadAudioUseCase @Inject constructor(
    private val repository: AudioRepository
) {
    suspend fun execute(filePaths: List<String>, deviceId: String, eventType: String): List<Boolean> {
        val results = mutableListOf<Boolean>()
        for (filePath in filePaths) {
            Log.d("UploadAudioUseCase", "Uploading audio: $filePath")
            val result = repository.uploadAudio(
                filePath = filePath,
                deviceId = deviceId,
                eventType = eventType
            )
            Log.d("UploadAudioUseCase", "Upload result: $result")
            results.add(result)
        }
        return results
    }
}
