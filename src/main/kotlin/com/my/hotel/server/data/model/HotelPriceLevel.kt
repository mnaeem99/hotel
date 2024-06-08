package com.my.hotel.server.data.model

import javax.persistence.*

@Entity
@Table(name = "hotel_price")
data class HotelPriceLevel(
    @Enumerated(EnumType.STRING)
    var name: HotelPrice?=null,
    @Id
    var id: Long? = null,
){
    enum class HotelPrice {
        Inexpensive,
        Moderate,
        Expensive,
        VeryExpensive
    }
}
