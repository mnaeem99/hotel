package com.my.hotel.server.service.notification.notificationType

import com.my.hotel.server.data.model.Notification
import com.my.hotel.server.data.model.User
import com.my.hotel.server.service.event.dto.Event
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class NotificationTypeFactory @Autowired constructor(
    val friendFavoriteType: FriendFavoriteType,
    val friendAddingWishlistType: FriendAddingWishlistType,
    val friendJoinedType: FriendJoinedType,
    val friendStatusType: FriendStatusType,
    val myStatusType: MyStatusType,
    val newFollowerType: NewFollowerType,
    val requestFollowingType: RequestFollowingType
){
    fun generateNotification(event: Event): Notification? {
        when (event.type) {
            friendFavoriteType.type -> {
                return friendFavoriteType.createNotification(event)
            }
            friendAddingWishlistType.type -> {
                return friendAddingWishlistType.createNotification(event)
            }
            friendJoinedType.type -> {
                return friendJoinedType.createNotification(event)
            }
            friendStatusType.type -> {
                return friendStatusType.createNotification(event)
            }
            myStatusType.type -> {
                return myStatusType.createNotification(event)
            }
            newFollowerType.type -> {
                return newFollowerType.createNotification(event)
            }
            requestFollowingType.type -> {
                return requestFollowingType.createNotification(event)
            }
            else -> {
                return null
            }
        }
    }

    fun findUsers(event: Event): List<User>? {
        when (event.type) {
            friendFavoriteType.type -> {
                return friendFavoriteType.findUsers(event)
            }
            friendAddingWishlistType.type -> {
                return friendAddingWishlistType.findUsers(event)
            }
            friendJoinedType.type -> {
                return friendJoinedType.findUsers(event)
            }
            friendStatusType.type -> {
                return friendStatusType.findUsers(event)
            }
            myStatusType.type -> {
                return myStatusType.findUsers(event)
            }
            newFollowerType.type -> {
                return newFollowerType.findUsers(event)
            }
            requestFollowingType.type -> {
                return requestFollowingType.findUsers(event)
            }
            else -> {
                return null
            }
        }
    }
}