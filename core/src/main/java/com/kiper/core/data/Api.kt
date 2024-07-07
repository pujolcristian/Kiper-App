package com.kiper.core.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @GET("getSchedule")
    suspend fun getSchedule(@Query("deviceId") deviceId: String): Response<ScheduleResponse>

    @Multipart
    @POST("command")
    suspend fun uploadAudio(
        @Part data: MultipartBody.Part,
        @Part("event") event: RequestBody,
        @Part("deviceId") deviceId: RequestBody,
    ): Response<Unit>

}

data class ScheduleResponse(
    val hours: List<Schedule>,
)

data class Schedule(
    val startTime: String,
    val endTime: String,
)
