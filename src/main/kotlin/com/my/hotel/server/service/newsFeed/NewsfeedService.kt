package com.my.hotel.server.service.newsFeed

import com.my.hotel.server.data.repository.DeviceLocationRepository
import com.my.hotel.server.data.repository.FavoriteRepository
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.response.NewsFeedDto
import com.my.hotel.server.graphql.dto.response.NewsFeeds
import com.my.hotel.server.graphql.dto.response.HotelUser
import com.my.hotel.server.provider.translation.TranslationService
import com.my.hotel.server.security.SecurityUtils
import org.locationtech.jts.geom.Point
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.stream.Collectors


@Service
class NewsfeedService @Autowired constructor(
    private val favoriteRepository: FavoriteRepository,
    private val translationService: TranslationService,
    private val deviceLocationRepository: DeviceLocationRepository,
): INewsfeedService {
    override fun getPopularUserHighlight(language: String, pageOptions: GraphQLPage): Page<NewsFeeds>? {
        val principal = SecurityUtils.getPrincipalUser()
        var userId: Long? = null
        var countryId: Long? = null
        var point: Point? = null
        if (principal!=null){
            userId = principal.id
            countryId = principal.country?.id
        }
        val deviceID = SecurityUtils.getLoggedInDevice()
        if (deviceID!=null) {
            val deviceLocation = deviceLocationRepository.findByIdOrNull(deviceID)
            if (deviceLocation != null) {
                point = deviceLocation.point
            }
        }
        val page = favoriteRepository.findMostAddedHotel(userId,countryId,point,language,pageOptions.toPageable())
        val hotelIDs = page.content.stream().map{f -> f.favorites?.hotel?.id}.collect(Collectors.toList())
        val hotelUsers = favoriteRepository.findUsersByHotel(hotelIDs)
        return page.map { entity -> toNewsFeeds(entity, hotelUsers) }
    }
    override fun getFriendsHighlight(language: String, pageOptions: GraphQLPage): Page<NewsFeeds>? {
        val principal = SecurityUtils.getLoggedInUser()
        val page = favoriteRepository.findFriendsAddedHotel(principal.id!!, language, pageOptions.toPageable())
        val hotelIDs = page.content.stream().map{f -> f.favorites?.hotel?.id}.collect(Collectors.toList())
        val hotelUsers = favoriteRepository.findUsersByHotel(hotelIDs)
        return page.map { entity -> toNewsFeeds(entity, hotelUsers) }
    }
    fun toNewsFeeds(newsFeeds: NewsFeedDto, HotelUsers: List<HotelUser>?): NewsFeeds {
        val filteredUsers = HotelUsers?.stream()?.filter{ hotelUser -> hotelUser.hotelId == newsFeeds.favorites?.hotel?.id }?.collect(Collectors.toList())?.take(5)
        val qualities = favoriteRepository.findhotelQualities(newsFeeds.favorites?.hotel?.id, null, GraphQLPage(0, 3).toPageable())
        val usersDto =  filteredUsers?.map { hotelUser -> translationService.mapUser(hotelUser.user, null) }?.stream()?.collect(Collectors.toList())
        return NewsFeeds(
            newsFeeds.favorites?.postTime,
            translationService.mapUser(newsFeeds.favorites?.user!!, null),
            newsFeeds.hotelTranslation?.name,
            usersDto,
            newsFeeds.favCount?.toInt(),
            newsFeeds.hotelTranslation?.address,
            newsFeeds.favorites.hotel.photo,
            qualities?.content,
            newsFeeds.favorites.hotel.id
        )
    }
}