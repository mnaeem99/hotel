package com.my.hotel.server.data.model

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "favorites")
data class Favorites(
    @ManyToOne
    var user: User,
    @ManyToOne
    var hotel: MyHotel,
    @ManyToMany
    @JoinColumn(name = "favorite_id")
    var quality: List<Quality>? = null,
    var postTime: LocalDateTime? = null,
    @Id
    @GeneratedValue
    var id: Long? = null,
)
