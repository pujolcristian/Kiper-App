package com.kiper.core.framework.audioRecorder

import java.io.File

interface AudioRecorder {
    fun start(outputFile: File)
    fun stop()
}