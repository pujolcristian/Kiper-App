package com.kiper.core.domain.repository

import com.kiper.core.domain.entity.AudioFile

interface AudioRepository {
    fun startRecording(fileName: String)
    fun stopRecording(): AudioFile
}