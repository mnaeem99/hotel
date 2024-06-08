package com.my.hotel.server.data.model

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "wishlists")
data class WishList(
    @ManyToOne
    var user: User,
    @ManyToOne
    var hotel: MyHotel,
    var createdAt: LocalDateTime? = null,
    @Id
    @GeneratedValue
    var id: Long? = null,
    var context: String?=null
)
