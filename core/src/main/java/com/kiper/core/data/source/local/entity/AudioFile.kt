package com.kiper.core.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AudioFile(
    @PrimaryKey val fileName: String,
    val scheduleStartTime: String,
    val scheduleEndTime: String,
    val recordedAt: Long,
    val isUploaded: Boolean = false
)
