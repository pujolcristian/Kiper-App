package com.kiper.core.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "audio_recordings")
data class AudioRecordingEntity(
    @PrimaryKey val fileName: String,
    val filePath: String,
    val startTime: Date,
    val duration: Long,
)
