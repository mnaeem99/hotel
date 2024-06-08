package com.my.hotel.server.service.status.dto

import com.my.hotel.server.data.model.Image
import com.my.hotel.server.data.model.StatusHistory
import java.time.LocalDate

data class StatusHistoryDto(
    val title: String?,
    val subTitle: String?,
    val statusTile: List<StatusHistory>?,
    val hotelImage: Image?,
    val spending: Double?,
    val date: LocalDate?
)