package com.my.hotel.server.data.model

import javax.persistence.*

@Entity
@Table(name = "hotel_price_ranges")
data class HotelPriceRange(
    var range: Int,
    @ManyToOne
    var user: User,
    @ManyToOne
    var hotel: MyHotel,
    @Id
    @GeneratedValue
    var id: Long? = null,
)