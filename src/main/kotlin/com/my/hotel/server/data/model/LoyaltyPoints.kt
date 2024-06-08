package com.my.hotel.server.data.model

import javax.persistence.*

@Entity
@Table(name = "loyal_points")
data class LoyaltyPoints(
    @ManyToOne
    var user: User,
    @ManyToOne
    var hotel: MyHotel,
    var loyaltyPoints: Int?,
    @Id
    @GeneratedValue
    var id: Long? = null,
)
