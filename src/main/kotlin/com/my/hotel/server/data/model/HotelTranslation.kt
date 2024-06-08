package com.my.hotel.server.data.model

import javax.persistence.*


@Entity
@Table(name = "my_hotels_translations")
data class HotelTranslation(
    var name: String?,
    var address: String?=null,
    var language: String?=null,
    @ManyToOne
    var hotel: MyHotel,
    @Id
    @GeneratedValue
    var id: Long? = null,
)