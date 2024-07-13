package com.kiper.core.data.mappers

import com.kiper.core.data.dto.ScheduleResponseDto
import com.kiper.core.data.source.local.entity.ScheduleEntity
import com.kiper.core.domain.model.ScheduleResponse

fun ScheduleResponseDto.toScheduleResponse() = ScheduleResponse(
    startTime = startTime,
    endTime = endTime
)

fun ScheduleResponseDto.toScheduleEntity() = ScheduleEntity(
    startTime = startTime,
    endTime = endTime
)

fun ScheduleEntity.toScheduleResponseDto() = ScheduleResponseDto(
    startTime = startTime,
    endTime = endTime
)