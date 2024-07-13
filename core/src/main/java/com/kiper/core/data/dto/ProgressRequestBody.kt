package com.kiper.core.data.dto

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File

class ProgressRequestBody(
    private val file: File,
    private val contentType: String,
    private val listener: (progress: Int) -> Unit
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
                listener((100 * uploaded / contentLength()).toInt())
            }
        }
    }
}
