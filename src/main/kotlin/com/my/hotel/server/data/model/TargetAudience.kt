package com.my.hotel.server.data.model

import javax.persistence.*

@Entity
@Table(name = "target_audience")
data class TargetAudience(
    var title: String?,
    @Id
    @GeneratedValue
    var id: Long? = null,
)
