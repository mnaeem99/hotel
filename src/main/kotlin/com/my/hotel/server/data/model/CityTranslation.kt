package com.my.hotel.server.data.model

import javax.persistence.*


@Entity
@Table(name = "city_translation")
data class CityTranslation(
    var name: String?,
    var language: String?=null,
    @ManyToOne
    var city: City,
    @Id
    @GeneratedValue
    var id: Long? = null,
)