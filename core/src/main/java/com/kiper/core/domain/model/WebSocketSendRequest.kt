package com.kiper.core.domain.model


import com.google.gson.annotations.SerializedName

data class WebSocketSendRequest(
    @SerializedName("clientId")
    val clientId: String? = null,
    @SerializedName("type")
    val type: String? = null
)