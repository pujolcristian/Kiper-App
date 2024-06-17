package com.kiper.core.util

import android.content.Context
import android.media.MediaRecorder
import com.kiper.core.domain.entity.AudioFile
import java.io.File

object AudioUtils {

    private var mediaRecorder: MediaRecorder? = null
    private var fileName: String = ""
    private var filePath: String = ""

    fun startRecording(context: Context, fileName: String) {
        this.fileName = fileName
        val audioDirectory = File(context.getExternalFilesDir(null), "MyRecordings")
        if (!audioDirectory.exists()) {
            audioDirectory.mkdirs()
        }
        filePath = "${audioDirectory.absolutePath}/$fileName.3gp"

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(filePath)
            prepare()
            start()
        }

        RecordingManager.startRecording()
    }

    fun stopRecording(): AudioFile {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        RecordingManager.stopRecording()
        return AudioFile(fileName, filePath)
    }
}
