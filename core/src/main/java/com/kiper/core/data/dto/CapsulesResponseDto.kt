package com.kiper.core.data.dto

import com.google.gson.annotations.SerializedName

data class CapsulesResponseDto(
    @SerializedName("messages")
    val messages: List<String>? = null
)