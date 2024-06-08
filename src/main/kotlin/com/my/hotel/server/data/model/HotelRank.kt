package com.my.hotel.server.data.model

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "hotel_rank")
data class HotelRank(
    @ManyToOne
    var hotel: MyHotel,
    var currentRank: Int,
    var previousRank: Int? = null,
    var updatedAt: LocalDateTime,
    @Id
    @GeneratedValue
    var id: Long? = null,
)
