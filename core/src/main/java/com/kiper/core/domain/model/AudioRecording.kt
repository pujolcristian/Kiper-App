package com.kiper.core.domain.model

import java.util.Date

data class AudioRecording(
    val fileName: String,
    val filePath: String,
    val startTime: Date,
    val duration: Long,
)

