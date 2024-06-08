package com.my.hotel.server.service.wishlist

import com.my.hotel.server.commons.Constants
import com.my.hotel.server.data.model.NotificationType
import com.my.hotel.server.data.model.User
import com.my.hotel.server.data.model.WishList
import com.my.hotel.server.data.repository.MyHotelRepository
import com.my.hotel.server.data.repository.UserRepository
import com.my.hotel.server.data.repository.WishListRepository
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.request.UserHotelFilter
import com.my.hotel.server.graphql.dto.request.WishListHotel
import com.my.hotel.server.graphql.dto.response.MyHotelDto
import com.my.hotel.server.graphql.error.NotFoundCustomException
import com.my.hotel.server.provider.dateProvider.DateProvider
import com.my.hotel.server.provider.translation.TranslationService
import com.my.hotel.server.security.SecurityUtils
import com.my.hotel.server.service.event.EventService
import com.my.hotel.server.service.event.dto.Event
import com.my.hotel.server.service.hotel.HotelDetailService
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
@Slf4j
class WishListService @Autowired constructor(
    private val wishListRepository: WishListRepository,
    private val hotelDetailService: HotelDetailService,
    private val eventService: EventService,
    private val dateProvider: DateProvider,
    private val userRepository: UserRepository,
    private val myHotelRepository: MyHotelRepository,
    private val translationService: TranslationService,
) : IWishListService {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)
    override fun getWishListHotel(filter: UserHotelFilter, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.UserAddedHotel>? {
        val user = userRepository.findByIdOrNull(filter.userId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "id")
        if (user.isPrivate == true){
            val principal = SecurityUtils.getLoggedInUser()
            if (userRepository.isFollowing(principal.id!!, user.id!!) != null
                || userRepository.isFollowing(user.id!!, principal.id!!) != null
                || principal.id == user.id){
                return wishListRepository.findByUserLocationKeyword(filter.userId, filter.countryId, filter.language ?: Constants.DEFAULT_LANGUAGE, filter.searchKeyword, pageOptions.toPageable())
            }
            return null
        }
        return wishListRepository.findByUserLocationKeyword(filter.userId, filter.countryId, filter.language ?: Constants.DEFAULT_LANGUAGE, filter.searchKeyword, pageOptions.toPageable())
    }
    override fun getLocationsOfWishlistHotel(userId: Long?, language: String?, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.PlaceHotelDto>? {
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "id")
        if (user.isPrivate == true){
            val principal = SecurityUtils.getLoggedInUser()
            if (userRepository.isFollowing(principal.id!!, user.id!!) != null
                || userRepository.isFollowing(user.id!!, principal.id!!) != null
                || principal.id == user.id){
                return myHotelRepository.findWishlisthotelLocation(userId, language?: Constants.DEFAULT_LANGUAGE, pageOptions.toPageable())
            }
            return null
        }
        return myHotelRepository.findWishlisthotelLocation(userId, language?: Constants.DEFAULT_LANGUAGE, pageOptions.toPageable())
    }
    override fun getUsersWhoAddedhotelToWishlists(hotelId: Long, language: String?, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.UserDto>? {
        var key = "-${hotelId}-${language}-${pageOptions.page}-${pageOptions.size}"
        pageOptions.sort.orders.forEach { order ->
            key = key.plus("-${order.property}-${order.direction.name}-${order.nullHandling.name}-${order.ignoreCase}")
        }
        val users = userRepository.findByWishlistHotel(hotelId, pageOptions.toPageable()).map { entity -> translationService.mapUserDto(entity, language) }
        return users
    }

    override fun addToWishlist(input: WishListHotel): Boolean {
        val principal = SecurityUtils.getLoggedInUser()
        val hotel = hotelDetailService.getmyHotel(input.hotelId,input.placeId, principal.language?: Constants.DEFAULT_LANGUAGE)
        val wishList = wishListRepository.findByUserAndHotel(principal, hotel)
        if (!input.removeHotel && wishList == null) {
            val newWishList = wishListRepository.save(WishList(principal, hotel, createdAt = dateProvider.getCurrentDateTime()))
            logger.info("New Wishlist Added: ${newWishList.id}")
            logger.info("${principal.firstName} ${principal.lastName} added: ${hotel.id} in wishlist") // working on this line
            eventService.createEvent(Event(NotificationType.FRIEND_ADDING_WISHLIST,principal.id, principal.id,hotel.id))
            return true
        }
        else if(input.removeHotel && wishList != null){
            logger.info("Removed from Wishlist: ${wishList.id}")
            logger.info("${principal.firstName} ${principal.lastName} removed: ${hotel.id} from wishlist") // working on this line
            wishListRepository.deleteById(wishList.id!!)
            return true
        }
        return false
    }
    override fun isOnWishlist(hotelDto: MyHotelDto): Boolean {
        val principal = SecurityContextHolder.getContext().authentication.principal
        if (principal !is User)
            return false
        return wishListRepository.findByUserAndHotel(principal.id!!, hotelDto.id, hotelDto.placeId) != null
    }
}