package com.kiper.core.data.dto

import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File

class ProgressRequestBody(
    private val file: File,
    private val contentType: String,
) : RequestBody() {

    override fun contentType() = contentType.toMediaTypeOrNull()

    override fun contentLength() = file.length()

    override fun writeTo(sink: BufferedSink) {
        val buffer = ByteArray(2048)
        val fileInputStream = file.inputStream()
        var uploaded: Long = 0

        fileInputStream.use { inputStream ->
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                uploaded += read
                sink.write(buffer, 0, read)
                Log.d("Progress", "${(100 * uploaded / contentLength()).toInt()}%")
            }
        }
    }
}
