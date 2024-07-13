package com.kiper.core.data

import com.kiper.core.data.dto.SchedulesResponseDto
import com.kiper.core.data.dto.base.BaseResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @GET("https://faf2e4aa-c2db-480b-8177-b8c32d9cf3d5.mock.pstmn.io/command/getSchedule")
    suspend fun getSchedule(@Query("deviceId") deviceId: String): Response<BaseResponse<SchedulesResponseDto>>

    @Multipart
    @POST("command")
    suspend fun uploadAudio(
        @Part data: MultipartBody.Part,
        @Part("event") event: RequestBody,
        @Part("deviceId") deviceId: RequestBody,
    ): Response<BaseResponse<String?>>



}
