package com.my.hotel.server.data.model

import javax.persistence.*

@Entity
@Table(name = "quality")
data class Quality(
    var name: String,
    @ManyToOne
    var qualityType: QualityType,
    @Id
    @GeneratedValue
    var id: Long? = null,
    var equivalentQuality: String?=null,
    var active: Boolean?=false
)
