package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.MyHotel
import com.my.hotel.server.data.model.Notification
import com.my.hotel.server.data.model.User
import com.my.hotel.server.graphql.dto.response.UserNotificationDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
interface NotificationRepository : JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {
    @Query("SELECT NEW com.my.hotel.server.graphql.dto.response.UserNotificationDto(n, un.status) " +
            "FROM UserNotification un " +
            "JOIN Notification n ON n.id = un.notification.id " +
            "WHERE un.user.id = :userId " +
            "      AND un.status = 'UNREAD' " +
            "      AND n.createdAt >= :sevenDaysAgo " +
            "      AND (n.text, n.createdAt) IN ( " +
            "          SELECT nn.text, MAX(nn.createdAt) " +
            "          FROM UserNotification un2 " +
            "          JOIN Notification nn ON nn.id = un2.notification.id " +
            "          WHERE un2.user.id = :userId " +
            "          GROUP BY nn.text " +
            "      ) " +
            "ORDER BY n.createdAt DESC")
    fun findUnReadNotification(userId: Long, sevenDaysAgo: LocalDateTime, pageable: Pageable): Page<UserNotificationDto>?

    @Query("select NEW com.my.hotel.server.graphql.dto.response.UserNotificationDto(n, un.status) from UserNotification un JOIN Notification n ON n.id = un.notification.id where un.user.id = :userId and ( un.status = 'READ' OR ( un.status = 'UNREAD' and n.createdAt <= :sevenDaysAgo ) ) AND (n.text, n.createdAt) IN ( SELECT nn.text, MAX(nn.createdAt) FROM UserNotification un2 JOIN Notification nn ON nn.id = un2.notification.id WHERE un2.user.id = :userId GROUP BY nn.text ) ORDER BY n.createdAt DESC")
    fun findByUser(userId: Long, sevenDaysAgo: LocalDateTime, pageable: Pageable): Page<UserNotificationDto>?

    @Modifying
    @Transactional
    @Query(value = "delete from users_notification where notification_id in ( " +
            "SELECT id from notification WHERE user_id = ?1 " +
            ") or user_id = ?1", nativeQuery = true)
    fun deleteByUser(user: Long?)

    @Modifying
    @Transactional
    @Query(value = "delete from users_notification where notification_id in ( " +
            "SELECT id from notification WHERE hotel_id = ?1 " +
            ")", nativeQuery = true)
    fun deleteByHotel(hotel: Long?)

    @Modifying
    @Transactional
    @Query("DELETE from Notification n WHERE n.user = ?1")
    fun deleteByContent(user: User?)
    @Modifying
    @Transactional
    @Query("DELETE from Notification n WHERE n.hotel = ?1")
    fun deleteByHotel(hotel: MyHotel?)
}
