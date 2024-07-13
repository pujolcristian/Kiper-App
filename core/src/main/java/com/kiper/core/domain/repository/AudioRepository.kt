package com.kiper.core.domain.repository

import com.kiper.core.domain.model.AudioRecording
import com.kiper.core.domain.model.ScheduleResponse
import kotlinx.coroutines.flow.Flow

interface AudioRepository {
    suspend fun getDeviceSchedules(deviceId: String): Flow<List<ScheduleResponse>?>
    suspend fun uploadAudio(filePath: String, deviceId: String, eventType: String): Boolean
    suspend fun saveRecording(recording: AudioRecording)
    suspend fun getRecordingsForDay(startOfDay: Long, endOfDay: Long): List<AudioRecording>
    suspend fun deleteRecordingUploaded(fileName: String)
}
