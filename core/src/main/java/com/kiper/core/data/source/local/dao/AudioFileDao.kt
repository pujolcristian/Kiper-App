package com.kiper.core.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kiper.core.data.source.local.entity.AudioFile

@Dao
interface AudioFileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudioFile(audioFile: AudioFile)

    @Query("SELECT * FROM AudioFile WHERE isUploaded = 0 ORDER BY recordedAt ASC")
    suspend fun getPendingUploads(): List<AudioFile>

    @Update
    suspend fun updateAudioFile(audioFile: AudioFile)

    @Query("DELETE FROM AudioFile WHERE isUploaded = 1")
    suspend fun deleteUploadedFiles()
}