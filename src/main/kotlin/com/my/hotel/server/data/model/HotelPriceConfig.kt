package com.my.hotel.server.data.model

import javax.persistence.*

@Entity
@Table(name = "hotel_price_config")
data class HotelPriceConfig(
    var priceLevelThreshold: Float? = null,
    var priceRangeUsers: Int? = null,
    @Id
    @GeneratedValue
    var id: Long? = null,
)