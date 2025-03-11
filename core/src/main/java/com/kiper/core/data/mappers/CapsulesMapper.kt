package com.kiper.core.data.mappers

import com.kiper.core.data.dto.CapsulesResponseDto
import com.kiper.core.domain.model.CapsulesResponse

fun CapsulesResponseDto.toCapsulesResponse(): CapsulesResponse {
    return CapsulesResponse(
        messages = messages
    )
}