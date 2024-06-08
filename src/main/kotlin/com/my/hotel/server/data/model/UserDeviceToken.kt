package com.my.hotel.server.data.model

import javax.persistence.*

@Entity
@Table(name = "user_device_token")
data class UserDeviceToken(
    @ManyToOne
    var user: User,
    var deviceToken: String,
    @Id
    @GeneratedValue
    var id: Long? = null,
)
