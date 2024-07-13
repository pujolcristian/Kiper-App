package com.kiper.core.domain.model

import com.google.gson.annotations.SerializedName


data class WebSocketEventResponse(
    @SerializedName("event")
    val event: String? = null,
)