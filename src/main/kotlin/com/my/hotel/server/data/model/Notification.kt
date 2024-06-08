package com.my.hotel.server.data.model

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "notification")
data class Notification(
    @Enumerated(EnumType.STRING)
    var title: NotificationType,
    var text: String?=null,
    @ManyToOne
    val user: User?=null,
    @ManyToOne
    val hotel: MyHotel?=null,
    @ManyToOne
    val status: Status?=null,
    var createdAt: LocalDateTime? = null,
    @Id
    @GeneratedValue
    var id: Long? = null,
)