package com.kiper.core.data.dto

import com.google.gson.annotations.SerializedName

data class UpdateDataResponseDto(
    @SerializedName("version") val version: String? = null,
    @SerializedName("url") val url: String? = null,
)