package com.kiper.core.data.repository

import com.kiper.core.data.mappers.toAudioRecording
import com.kiper.core.data.mappers.toAudioRecordingEntity
import com.kiper.core.data.mappers.toScheduleResponse
import com.kiper.core.data.source.remote.AudioLocalDataSource
import com.kiper.core.data.source.remote.AudioRemoteDataSource
import com.kiper.core.domain.model.AudioRecording
import com.kiper.core.domain.model.ScheduleResponse
import com.kiper.core.domain.repository.AudioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AudioRepositoryImpl @Inject constructor(
    private val audioRemoteDataSource: AudioRemoteDataSource,
    private val audioLocalDataSource: AudioLocalDataSource
    ) : AudioRepository {

    override suspend fun getDeviceSchedules(deviceId: String): Flow<List<ScheduleResponse>?> {
        val response = audioRemoteDataSource.getDeviceSchedules(deviceId)
        return response.map { it?.map { value -> value.toScheduleResponse() } }
    }

    override suspend fun uploadAudio(filePath: String, deviceId: String, eventType: String): Boolean {
        return audioRemoteDataSource.uploadAudio(
            filePath = filePath,
            deviceId = deviceId,
            eventType = eventType
        )
    }

    override suspend fun saveRecording(recording: AudioRecording) {
        println("Saving recording: $recording")
        audioLocalDataSource.saveRecording(recording.toAudioRecordingEntity())
    }

    override suspend fun getRecordingsForDay(
        startOfDay: Long,
        endOfDay: Long,
    ): List<AudioRecording> {
        println("Getting recordings for day: $startOfDay - $endOfDay")
        val response = audioLocalDataSource.getRecordingsForDay(startOfDay, endOfDay)
        println("Response: $response")
        return response.map { it.toAudioRecording() }
    }

    override suspend fun deleteRecordingUploaded(fileName: String) {
        println("Deleting recording with id: $fileName")
        audioLocalDataSource.deleteRecordingUploaded(fileName = fileName)
    }
}
