package com.kiper.core.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Schedule(
    @PrimaryKey val startTime: String,
    val endTime: String
)