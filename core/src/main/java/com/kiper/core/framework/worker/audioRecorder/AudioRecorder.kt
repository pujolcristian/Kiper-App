package com.kiper.core.framework.worker.audioRecorder

import java.io.File

interface AudioRecorder {
    fun start(outputFile: File)
    fun stop()
}