package com.my.hotel.server.data.model

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "status")
data class Status(
    var name: String? = null,
    var percentage: Int? = null,
    @Id
    @GeneratedValue
    var id: Long? = null,
)