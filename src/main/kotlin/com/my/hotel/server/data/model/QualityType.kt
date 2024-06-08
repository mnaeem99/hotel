package com.my.hotel.server.data.model

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table


@Entity
@Table(name = "quality_type")
data class QualityType(
    var name: String,
    @Id
    @GeneratedValue
    var id: Long? = null,
)
