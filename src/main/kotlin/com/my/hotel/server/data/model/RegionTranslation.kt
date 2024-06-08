package com.my.hotel.server.data.model

import javax.persistence.*


@Entity
@Table(name = "my_region_translation")
data class RegionTranslation(
    var name: String?,
    var address: String?=null,
    var language: String?=null,
    @ManyToOne
    var region: Region,
    @Id
    @GeneratedValue
    var id: Long? = null,
)