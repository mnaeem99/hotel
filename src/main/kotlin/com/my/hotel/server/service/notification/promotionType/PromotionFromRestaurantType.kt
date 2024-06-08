package com.my.hotel.server.service.notification.promotionType

import com.my.hotel.server.data.model.Promotion
import com.my.hotel.server.data.model.User
import com.my.hotel.server.data.repository.PromotionRepository
import com.my.hotel.server.data.repository.UserRepository
import com.my.hotel.server.service.event.dto.Event
import com.my.hotel.server.service.notification.notificationType.NotificationSettingService
import com.my.hotel.server.service.hotel.RegionDetailService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.stream.Collectors

@Component
class PromotionFromhotelType @Autowired constructor(
    private val userRepository: UserRepository,
    private val promotionRepository: PromotionRepository,
    private val notificationSettingService: NotificationSettingService,
    private val regionDetailService: RegionDetailService
) {
    fun getPromotion(event: Event): Promotion? {
        return promotionRepository.findByIdOrNull(event.otherInfo)
    }
    fun findUsers(event: Event): List<User>? {
        val users = ArrayList<User>()
        val promotion = promotionRepository.findByIdOrNull(event.otherInfo) ?: return null
        if (promotion.geolat!=null && promotion.geolong != null && promotion.radius!=null) {
            val countryId = regionDetailService.getGoogleCountry(
                com.my.hotel.server.graphql.dto.request.QueryFilter(
                    null,
                    promotion.geolat!!.toDouble(),
                    promotion.geolong!!.toDouble()
                )
            )
            val locationUsers = userRepository.findByLocation(null,countryId,null, Pageable.unpaged())
            locationUsers.let { users.addAll(it.content) }
        }
        if(!promotion.targetAudience.isNullOrEmpty()){
            val targetAudiences = getTargetAudience(promotion)
            users.addAll(targetAudiences)
        }
        val userId = users.stream().map { user -> user.id }.collect(Collectors.toList())
        return userRepository.findAllById(userId).filter { entity -> checkNotificationSettings(entity) }.stream().collect(Collectors.toList())
    }

    fun getTargetAudience(promotion: Promotion): ArrayList<User> {
        val users = ArrayList<User>()
        for (targetAudience in promotion.targetAudience!!) {
            if (targetAudience.id == 1L) {
                val favorites = userRepository.findByFavoriteHotel(promotion.hotel.id!!, null, Pageable.unpaged())
                favorites.content.let { users.addAll(it) }
            }
            if (targetAudience.id == 2L) {
                val wishList = userRepository.findByWishlistHotel(promotion.hotel.id!!, Pageable.unpaged())
                wishList.content.let { users.addAll(it) }
            }
            if (targetAudience.id == 3L) {
                val favorites = userRepository.findByFavoriteHotel(promotion.hotel.id!!, null, Pageable.unpaged())
                val wishList = userRepository.findByWishlistHotel(promotion.hotel.id!!, Pageable.unpaged())
                favorites.content.stream().map { user -> users.addAll(user.followers) }?.collect(Collectors.toList())
                wishList.content.stream().map { user -> users.addAll(user.followers) }?.collect(Collectors.toList())
            }
        }
        return users
    }

    private fun checkNotificationSettings(user: User): Boolean {
        val settingUser = notificationSettingService.createNotificationSettings(user)
        return settingUser.notificationSetting?.pauseAll == false && settingUser.notificationSetting?.promotionFromHotel == false
    }
}