package com.kiper.core.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kiper.core.data.source.local.entity.AudioRecordingEntity

@Dao
interface AudioRecordingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: AudioRecordingEntity)

    @Query("SELECT * FROM audio_recordings WHERE startTime >= :startOfDay AND startTime <= :endOfDay ORDER BY startTime")
    suspend fun getRecordingsForDay(startOfDay: Long, endOfDay: Long): List<AudioRecordingEntity>

    @Query("DELETE FROM audio_recordings WHERE fileName = :fileName")
    suspend fun deleteRecordingUploaded(fileName: String)

    @Query("DELETE FROM audio_recordings")
    suspend fun deleteAll()

}
