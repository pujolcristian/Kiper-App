package com.kiper.core.data.dto

import com.google.gson.annotations.SerializedName

data class SchedulesResponseDto(
    @SerializedName("hours")
    val hours: List<ScheduleResponseDto>,
)
data class ScheduleResponseDto(
    @SerializedName("startTime")
    val startTime: String,
    @SerializedName("endTime")
    val endTime: String,
)