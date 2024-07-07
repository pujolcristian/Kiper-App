package com.kiper.core.data.repository

import com.kiper.core.data.ApiService
import com.kiper.core.data.ScheduleResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import java.io.File
import javax.inject.Inject

class Repository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getDeviceSchedules(deviceId: String): Response<ScheduleResponse> {
        return apiService.getSchedule(deviceId)
    }

    suspend fun uploadAudio(filePath: String, deviceId: String) {
        val file = File(filePath)
        val requestFile = RequestBody.create(MultipartBody.FORM, file)
        val body = MultipartBody.Part.createFormData("data", file.name, requestFile)
        val deviceIdBody = RequestBody.create(MultipartBody.FORM, deviceId)

        val response = apiService.uploadAudio(
            body,
            RequestBody.create(MultipartBody.FORM, "processAudio"),
            deviceIdBody
        )
        println("Response: $response")
        if (response.isSuccessful) {
            file.delete()
        }
    }
}
