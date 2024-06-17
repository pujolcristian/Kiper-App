package com.kiper.core.util

object RecordingManager {
    private var isRecording = false

    fun startRecording() {
        isRecording = true
    }

    fun stopRecording() {
        isRecording = false
    }

    fun isRecording(): Boolean {
        return isRecording
    }
}