package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.Notification
import com.my.hotel.server.data.model.User
import com.my.hotel.server.data.model.UserNotification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface UserNotificationRepository : JpaRepository<UserNotification, Long>, JpaSpecificationExecutor<UserNotification>{
    fun findByUserAndNotification(user: User, notification: Notification) : UserNotification?
    @Query("SELECT un from UserNotification un where un.notification.title = 'REQUEST_FOLLOWING' and un.notification.user.id = :senderUser and un.user.id = :receivedUser")
    fun findFollowRequestNotification(senderUser: Long, receivedUser: Long): UserNotification?
    @Modifying
    @Transactional
    @Query("DELETE from UserNotification where id IN ( SELECT un.id from UserNotification un where un.notification.title = 'REQUEST_FOLLOWING' and un.notification.user.id = :senderUser and un.user.id = :receivedUser ) ")
    fun deleteFollowRequestNotification(senderUser: Long, receivedUser: Long)
}