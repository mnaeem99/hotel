package com.my.hotel.server.data.model

import javax.persistence.*


@Entity
@Table(name = "country_translation")
data class CountryTranslation(
    var name: String?,
    var language: String?=null,
    @ManyToOne
    var country: Country,
    @Id
    @GeneratedValue
    var id: Long? = null,
)