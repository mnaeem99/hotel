package com.my.hotel.server.service.qrCode.dto

import com.my.hotel.server.data.model.Order
import java.time.LocalDate

data class OrderDetails(
    var amount: Double?,
    var points: Int?,
    var hotelId: Long,
    var userId: Long,
    var orderType: Order.OrderType?,
    var createdAt: LocalDate?,
    var updatedAt: LocalDate?,
)