package com.kiper.core.data.source.remote

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.kiper.core.data.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class UpdateAppRemoteDataSource(
    private val apiService: ApiService,
    private val context: Context
) {

    suspend fun checkAndDownloadUpdate(): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getVersionInfo()
            if (response.isSuccessful) {
                val updateInfo = response.body()?.data
                val currentVersion =
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName.toDouble()
                Log.d("UpdateInfo", "Update info: ${updateInfo?.version?.toDouble()}, current version: $currentVersion")

                if ((updateInfo?.version?.toDouble() ?: 0.0) > currentVersion) {
                    updateInfo?.url?.let { downloadApk(it) }
                }
                return@withContext true
            } else {
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e("Update", "Failed to check for update: $e")
            return@withContext false
        }
    }

    private fun downloadApk(url: String) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            Log.d("Update2", "Response: $response")
            if (!response.isSuccessful) throw Exception("Failed to download file")

            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "app-release.apk")
            response.body?.let { body ->
                saveFile(body.byteStream(), file)
                installApk(file)
            }
        }
    }

    private fun saveFile(inputStream: InputStream, file: File) {
        Log.d("Update2", "Installing apk: $file")
        var outputStream: OutputStream? = null
        try {
            outputStream = FileOutputStream(file)
            val buffer = ByteArray(4 * 1024)
            var byteCount = inputStream.read(buffer)

            while (byteCount != -1) {
                outputStream.write(buffer, 0, byteCount)
                byteCount = inputStream.read(buffer)
            }
            outputStream.flush()
        } finally {
            inputStream.close()
            outputStream?.close()
        }
    }

    @SuppressLint("WearRecents")
    private fun installApk(file: File) {
        Log.d("Update3", "Installing apk: $file")
        val apkUri =
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
