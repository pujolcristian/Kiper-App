package com.kiper.core.domain.model

import com.google.gson.annotations.SerializedName

data class WebSocketMessageRequest(
    @SerializedName("clientId")
    val clientId: String,
    @SerializedName("message")
    val message: String = "Petición recibida",
    @SerializedName("to")
    val to: String = "messenger",
    @SerializedName("type")
    val type: String = "message"
)