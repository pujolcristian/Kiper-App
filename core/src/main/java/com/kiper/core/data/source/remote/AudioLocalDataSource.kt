package com.kiper.core.data.source.remote

import com.kiper.core.data.source.local.dao.AudioRecordingDao
import com.kiper.core.data.source.local.entity.AudioRecordingEntity
import javax.inject.Inject

class AudioLocalDataSource @Inject constructor(
    private val audioRecordingDao: AudioRecordingDao,
) {
    suspend fun saveRecording(recording: AudioRecordingEntity) {
        println("Saving recording: $recording")
        audioRecordingDao.insertRecording(recording)
    }

    suspend fun getRecordingsForDay(
        startOfDay: Long,
        endOfDay: Long,
    ): List<AudioRecordingEntity> {
        println("Getting recordings for day: $startOfDay - $endOfDay")
        val response = audioRecordingDao.getRecordingsForDay(startOfDay, endOfDay)
        println("Response: $response")
        return response
    }

    suspend fun deleteRecordingUploaded(fileName: String) {
        println("Deleting recording with id: $fileName")
        audioRecordingDao.deleteRecordingUploaded(fileName = fileName.replace(".3gp", ""))
    }

}