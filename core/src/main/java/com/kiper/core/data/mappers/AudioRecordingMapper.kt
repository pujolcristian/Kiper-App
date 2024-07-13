package com.kiper.core.data.mappers

import com.kiper.core.data.source.local.entity.AudioRecordingEntity
import com.kiper.core.domain.model.AudioRecording


fun AudioRecordingEntity.toAudioRecording() = AudioRecording(
    fileName = fileName,
    filePath = filePath,
    startTime = startTime,
    duration = duration
)

fun AudioRecording.toAudioRecordingEntity() = AudioRecordingEntity(
    fileName = fileName,
    filePath = filePath,
    startTime = startTime,
    duration = duration
)