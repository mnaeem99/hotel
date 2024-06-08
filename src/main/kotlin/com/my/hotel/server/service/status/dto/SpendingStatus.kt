package com.my.hotel.server.service.status.dto

import com.my.hotel.server.data.model.Status

data class SpendingStatus (
    val spenderPercentage: Int?,
    val statuses: List<Status>?
)