package com.my.hotel.server.data.model

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "suggestion")
data class Suggestion(
    @ManyToOne
    var user: User,
    @ManyToOne
    var myHotel: MyHotel?=null,
    @ManyToOne
    var googleHotel: GoogleHotel?=null,
    var createdAt: LocalDateTime? = null,
    @Id
    @GeneratedValue
    var id: Long? = null,
)
