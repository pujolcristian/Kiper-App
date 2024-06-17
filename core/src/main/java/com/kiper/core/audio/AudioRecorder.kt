package com.kiper.core.audio

import android.media.MediaRecorder

class AudioRecorder {
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording: Boolean = false

    fun startRecording(outputFile: String) {
        if (isRecording) return

        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile)
                prepare()
                start()
            }
            isRecording = true
        } catch (e: Exception) {
            e.printStackTrace()
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
        }
    }

    fun stopRecording() {
        if (!isRecording) return

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } finally {
            mediaRecorder = null
            isRecording = false
        }
    }
}
