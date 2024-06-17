package com.kiper.core.data.repository

import android.content.Context
import com.kiper.core.domain.entity.AudioFile
import com.kiper.core.domain.repository.AudioRepository
import com.kiper.core.util.AudioUtils

class AudioRepositoryImpl(private val context: Context) : AudioRepository {

    override fun startRecording(fileName: String) {
        AudioUtils.startRecording(context, fileName)
    }

    override fun stopRecording(): AudioFile {
        return AudioUtils.stopRecording()
    }
}