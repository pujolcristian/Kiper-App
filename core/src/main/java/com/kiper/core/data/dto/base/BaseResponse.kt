package com.kiper.core.data.dto.base

import com.google.gson.annotations.SerializedName
import com.kiper.core.util.Constants.ERROR_UPLOAD

data class BaseResponse<T>(
    @SerializedName("data")
    val data: T? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("status")
    val status: Boolean? = null,
) {
    fun isError() = data?.toString()?.lowercase()?.contains(ERROR_UPLOAD)
}