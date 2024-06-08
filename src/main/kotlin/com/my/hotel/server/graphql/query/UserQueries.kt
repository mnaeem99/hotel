package com.my.hotel.server.graphql.query

import com.my.hotel.server.data.repository.*
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.request.*
import com.my.hotel.server.graphql.dto.response.*
import com.my.hotel.server.service.explore.ExploreService
import com.my.hotel.server.service.favorites.FavoriteService
import com.my.hotel.server.service.findFriends.FindFriendService
import com.my.hotel.server.service.follow.FollowService
import com.my.hotel.server.service.notification.NotificationService
import com.my.hotel.server.service.qrCode.QRCodeService
import com.my.hotel.server.service.status.StatusService
import com.my.hotel.server.service.status.dto.SpendingStatus
import com.my.hotel.server.service.status.dto.StatusHistoryDto
import com.my.hotel.server.service.user.UserService
import com.my.hotel.server.service.wishlist.WishListService
import graphql.kickstart.tools.GraphQLQueryResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component

@Component
class UserQueries @Autowired constructor(
    private val findFriendService: FindFriendService,
    private val qrCodeService: QRCodeService,
    private val statusService: StatusService,
    private val notificationService: NotificationService,
    private val exploreService: ExploreService,
    private val userService: UserService,
    private val followService: FollowService,
    private val favoriteService: FavoriteService,
    private val wishListService: WishListService
) : GraphQLQueryResolver{

    @PreAuthorize("hasAnyAuthority('USER')")
    fun getProfile(language: String?): UserProfile {
        return userService.getProfile(language)
    }
    fun getUser(id: Long, language: String?): UserDto? {
        return userService.getUser(id, language)
    }
    fun getFollowers(filter: SearchUserFilter, pageOptions: GraphQLPage): Page<UserDto>? {
        return followService.getFollowers(filter,pageOptions)
    }
    fun getFollowing(filter: SearchUserFilter, pageOptions: GraphQLPage): Page<UserDto>? {
        return followService.getFollowing(filter,pageOptions)
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun getFollowRequest(language: String?, pageOptions: GraphQLPage): Page<UserDto>? {
        return followService.getFollowRequest(language,pageOptions)
    }
    fun getUsersWhoAddedhotelToFavorites(hotelId: Long, language: String?, pageOptions: GraphQLPage): Page<UserDto>? {
        return favoriteService.getUsersWhoAddedhotelToFavorites(hotelId, language, pageOptions)
    }
    fun getUsersWhoAddedhotelToWishlists(hotelId: Long, language: String?, pageOptions: GraphQLPage): Page<UserDto>? {
        return wishListService.getUsersWhoAddedhotelToWishlists(hotelId, language, pageOptions)
    }
    fun getUsersWhoAddedQuality(hotelId: Long, qualityId: Long, language: String?, pageOptions: GraphQLPage): Page<UserDto>? {
        return favoriteService.getUsersWhoAddedQuality(hotelId, qualityId, language, pageOptions)
    }
    fun getPopularUser(language: String?, keyword: String?, pageOptions: GraphQLPage): Page<UserDto>? {
        return findFriendService.getPopularUser(language, keyword, pageOptions)
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun getMyUserFromContacts(phones: List<String>, language: String?, keyword: String?, pageOptions: GraphQLPage): Page<UserDto>? {
        return findFriendService.getMyUserFromContacts(phones, language, keyword, pageOptions)
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun getMyUserFromFacebook(facebookId: List<String>, language: String?, keyword: String?, pageOptions: GraphQLPage): Page<UserDto>? {
        return findFriendService.getMyUserFromFacebook(facebookId, language, keyword, pageOptions)
    }
    fun suggestUser(language: String?, pageOptions: GraphQLPage): Page<UserDto>? {
        return findFriendService.getSuggestUser(language, pageOptions)
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun getLoyaltyPoints(language: String?, pageOptions: GraphQLPage): Page<LoyaltyPointsDto>? {
        return statusService.getLoyaltyPoints(language, pageOptions)
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun getPointsHistory(language: String?, pageOptions: GraphQLPage): Page<PointsHistoryDto>? {
        return statusService.getPointsHistory(language, pageOptions)
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun getNewNotification(language: String?): Page<NotificationDto>? {
        return notificationService.getNewNotification(language)
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun getNotification(language: String?, pageOptions: GraphQLPage): Page<NotificationDto>? {
        return notificationService.getNotification(language, pageOptions)
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun getPromotion(language: String?, pageOptions: GraphQLPage): Page<PromotionDto>? {
        return notificationService.getPromotion(language, pageOptions)
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun generateQRCode(userId: Long, giftId: Long): String? {
        return qrCodeService.getQRCode(userId, giftId)
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun getStatus(): SpendingStatus {
        return statusService.getStatus()
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun getStatusHistory(language: String?): List<StatusHistoryDto>? {
        return statusService.getStatusHistory(language)
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun searchUserByNickname(nickName: String, language: String?): UserDto? {
        return userService.searchUserByNickname(nickName, language)
    }
    fun searchUser(filters: QueryFilter, pageOptions: GraphQLPage): Page<UserDto>?{
        return exploreService.searchUser(filters,pageOptions)
    }

}