package com.my.hotel.server.data.model

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "follow_request")
data class FollowRequest(
        @ManyToOne
        var follower: User,
        @ManyToOne
        var following: User,
        var timestamp: LocalDateTime,
        @Id
        @GeneratedValue
        var id: Long? = null
    )