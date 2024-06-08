package com.my.hotel.server.data.model

import javax.persistence.*

@Entity
@Table(name = "points_history")
data class PointsHistory(
    @ManyToOne
    var user: User,
    @ManyToOne
    var hotel: MyHotel,
    var pointsSpent: Int?,
    @Id
    @GeneratedValue
    var id: Long? = null,
)
