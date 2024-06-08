package com.my.hotel.server.service.findFriends

import com.my.hotel.server.commons.SpatialUtils
import com.my.hotel.server.data.model.UserAuthentication
import com.my.hotel.server.data.repository.DeviceLocationRepository
import com.my.hotel.server.data.repository.UserRepository
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.response.UserDto
import com.my.hotel.server.provider.translation.TranslationService
import com.my.hotel.server.security.SecurityUtils
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.Point
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class FindFriendService @Autowired constructor(
    private val userRepository: UserRepository,
    private val deviceLocationRepository: DeviceLocationRepository,
    private val translationService: TranslationService,
) : IFindFriendService{
    override fun getPopularUser(language: String?, keyword: String?, pageOptions: GraphQLPage): Page<UserDto>? {
        val userId = SecurityUtils.getLoggedInUserId()
        val point: Point? = getLocation()
        return userRepository.findPopularUser(userId, point, keyword, pageOptions.toPageable())
            .map { entity -> translationService.mapUserDto(entity, language) }
    }
    override fun getMyUserFromContacts(phones: List<String>, language: String?, keyword: String?, pageOptions: GraphQLPage): Page<UserDto>? {
        val validPhones: List<String> = phones.stream().map { entity -> formatPhone(entity) }.collect(Collectors.toList())
        val principal = SecurityUtils.getLoggedInUser()
        return userRepository.findByPhones(validPhones, principal.id, keyword, pageOptions.toPageable()).map { entity -> translationService.mapUserDto(entity, language) }
    }
    fun formatPhone(phone: String): String {
        if (phone.startsWith("+"))
            return phone
        val user = userRepository.findByPhone("+$phone")
        if (user!=null)
            return "+$phone"
        else{
            val userAuths = userRepository.searchByPhone(phone)
            if (!userAuths.isNullOrEmpty()){
                val userAuth = userAuths.find { userAuthentication -> userAuthentication.type == UserAuthentication.Type.PHONE && userAuthentication.verified == true}
                return userAuth?.phone ?: ""
            }
            return "+$phone"
        }
    }
    override fun getMyUserFromFacebook(facebookId: List<String>, language: String?, keyword: String?, pageOptions: GraphQLPage): Page<UserDto>? {
        val principal = SecurityUtils.getLoggedInUser()
        return userRepository.findByFacebook(facebookId, principal.id, keyword, pageOptions.toPageable()).map { entity -> translationService.mapUserDto(entity, language) }
    }
    override fun getSuggestUser(language: String?, pageOptions: GraphQLPage): Page<UserDto>? {
        val userId = SecurityUtils.getLoggedInUserId()
        val point: Point? = getLocation()
        var circle: Geometry? = null
        if (point!=null){
            circle = SpatialUtils.createCircle(point.y, point.x, 1.0)
        }
        return userRepository.findSuggestedUsers(userId, circle, point, pageOptions.toPageable())
            .map { entity -> translationService.mapUserDto(entity, language) }
    }
    private fun getLocation(): Point? {
        var point: Point? = null
        val deviceID = SecurityUtils.getLoggedInDevice()
        if (deviceID != null) {
            val deviceLocation = deviceLocationRepository.findByIdOrNull(deviceID)
            if (deviceLocation != null) {
                point = deviceLocation.point
            }
        }
        return point
    }

}