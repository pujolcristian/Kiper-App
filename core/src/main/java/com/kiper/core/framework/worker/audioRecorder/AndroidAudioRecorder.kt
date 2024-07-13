package com.kiper.core.framework.worker.audioRecorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidAudioRecorder @Inject constructor(
    private val context: Context,
) : AudioRecorder {
    private var isRecording: Boolean = false
    private var recorder: MediaRecorder? = null

    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else MediaRecorder()
    }

    override fun start(outputFile: File) {
        if (isRecording) return

        try {
            createRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(FileOutputStream(outputFile).fd)
                prepare()
                start()
                recorder = this
            }
            isRecording = true
        } catch (e: Exception) {
            e.printStackTrace()
            recorder?.release()
            recorder = null
        }
    }

    override fun stop() {
        if (!isRecording) return

        try {
            recorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } finally {
            recorder = null
            isRecording = false
        }
    }
}
