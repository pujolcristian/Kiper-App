package com.kiper.core.data.source.local

import com.kiper.core.data.source.local.dao.AudioRecordingDao
import com.kiper.core.data.source.local.entity.AudioRecordingEntity
import javax.inject.Inject

class AudioLocalDataSource @Inject constructor(
    private val audioRecordingDao: AudioRecordingDao,
) {
    suspend fun saveRecording(recording: AudioRecordingEntity) {
        audioRecordingDao.insertRecording(recording)
    }

    suspend fun getRecordingsForDay(
        startOfDay: Long,
        endOfDay: Long,
    ): List<AudioRecordingEntity> {
        val response = audioRecordingDao.getRecordingsForDay(startOfDay, endOfDay)
        return response
    }

    suspend fun deleteRecordingUploaded(fileName: String) {
        audioRecordingDao.deleteRecordingUploaded(fileName = fileName.replace(".3gp", ""))
    }

    suspend fun deleteAllRecordings() {
        audioRecordingDao.deleteAll()
    }

}