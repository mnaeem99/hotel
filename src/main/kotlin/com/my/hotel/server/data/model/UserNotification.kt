package com.my.hotel.server.data.model

import javax.persistence.*

@Entity
@Table(name = "users_notification")
data class UserNotification(
        @ManyToOne
        var user: User,
        @ManyToOne
        var notification: Notification,
        @Enumerated(EnumType.STRING)
        var status: NotificationStatus?= NotificationStatus.UNREAD,
        @Id
        @GeneratedValue
        var id: Long? = null
){
        enum class NotificationStatus{ READ, UNREAD }
}