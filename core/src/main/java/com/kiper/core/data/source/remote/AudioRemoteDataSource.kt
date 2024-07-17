package com.kiper.core.data.source.remote

import com.kiper.core.data.ApiService
import com.kiper.core.data.dto.ProgressRequestBody
import com.kiper.core.data.dto.ScheduleResponseDto
import com.kiper.core.data.mappers.toScheduleEntity
import com.kiper.core.data.mappers.toScheduleResponseDto
import com.kiper.core.data.source.local.dao.ScheduleDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class AudioRemoteDataSource @Inject constructor(
    private val apiService: ApiService,
    private val localDataSource: AudioLocalDataSource,
    private val scheduleDao: ScheduleDao,
) {
    suspend fun getDeviceSchedules(deviceId: String): Flow<List<ScheduleResponseDto>?> {
        val response = apiService.getSchedule(deviceId)
        println("response: $response")
        if (response.isSuccessful) {
            println("response: ${response.body()?.data}")

            scheduleDao.deleteAll()
            response.body()?.data?.hours?.map { it.toScheduleEntity() }?.let {
                scheduleDao.saveAll(it)
            }

            return flow { emit(response.body()?.data?.hours) }
        } else {
            return scheduleDao.getAll().map { data -> data.map { it.toScheduleResponseDto() } }
        }
    }

    suspend fun uploadAudio(filePath: String, deviceId: String, eventType: String): Boolean {
        val file = File(filePath)
        println("Uploading audio: $filePath")
        val requestFile = ProgressRequestBody(file, "audio/3gp")
        val body = MultipartBody.Part.createFormData("data", file.name, requestFile)
        val event = eventType.toRequestBody("text/plain".toMediaTypeOrNull())
        val deviceIdBody = deviceId.toRequestBody("text/plain".toMediaTypeOrNull())

        val response = apiService.uploadAudio(data = body, event = event, deviceId = deviceIdBody)
        val result = response.isSuccessful && response.body()?.isError() == false
        if (result) {
            localDataSource.deleteRecordingUploaded(file.name)
        }
        return result
    }
}
