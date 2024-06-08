package com.my.hotel.server.data.model

import javax.persistence.*


@Entity
@Table(name = "locality_translation")
data class LocalityTranslation(
    var name: String?,
    var language: String?=null,
    @ManyToOne
    var locality: Locality,
    @Id
    @GeneratedValue
    var id: Long? = null,
)