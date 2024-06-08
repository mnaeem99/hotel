package com.my.hotel.server.data.model

import javax.persistence.*

@Entity
@Table(name = "admin")
data class Admin(
    var username : String? = null,
    var password : String? = null,
    @Id
    @GeneratedValue
    var id: Long? = null,
)