package com.my.hotel.server.data.model

import javax.persistence.*

@Entity
@Table(name = "notification_setting")
data class NotificationSetting(
    var pauseAll: Boolean = false,
    var newFollower: Boolean = false,
    var friendStatus: Boolean = false,
    var friendFavorite: Boolean = false,
    var friendJoined: Boolean = false,
    var friendAddingWishlist: Boolean = false,
    var promotionFromHotel: Boolean = false,
    var newsfeedAlert: Boolean = false,
    @Id
    @Column(name = "user_id", unique = true, nullable = false)
    val id: Long,
    @MapsId
    @OneToOne
    @JoinColumn(name = "user_id")
    val user:User
)