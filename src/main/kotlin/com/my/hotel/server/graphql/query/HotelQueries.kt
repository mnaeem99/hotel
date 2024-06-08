package com.my.hotel.server.graphql.query


import com.my.hotel.server.commons.Constants
import com.my.hotel.server.data.model.Quality
import com.my.hotel.server.data.model.QualityType
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.request.LocationFilter
import com.my.hotel.server.graphql.dto.request.QueryFilter
import com.my.hotel.server.graphql.dto.request.UserHotelFilter
import com.my.hotel.server.graphql.dto.response.*
import com.my.hotel.server.service.explore.ExploreService
import com.my.hotel.server.service.favorites.FavoriteService
import com.my.hotel.server.service.newsFeed.NewsfeedService
import com.my.hotel.server.service.quality.QualityService
import com.my.hotel.server.service.hotel.*
import com.my.hotel.server.service.wishlist.WishListService
import graphql.kickstart.tools.GraphQLQueryResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component

@Component
class HotelQueries @Autowired constructor(
    private val hotelAutoCompleteService: HotelAutoCompleteService,
    private val regionAutoCompleteService: RegionAutoCompleteService,
    private val hotelDetailService: HotelDetailService,
    private val regionDetailService: RegionDetailService,
    private val hotelSearchService: HotelSearchService,
    private val exploreService: ExploreService,
    private val newsfeedService: NewsfeedService,
    private val favoriteService: FavoriteService,
    private val wishListService: WishListService,
    private val qualityService: QualityService,
) : GraphQLQueryResolver {
    fun gethotelInner(hotelId: Long, language: String?): MyHotelDto?{
        return hotelDetailService.gethotelInner(hotelId, language?:Constants.DEFAULT_LANGUAGE)
    }
    fun similarHotel(hotelId: Long, language: String?, pageOptions: GraphQLPage): Page<MyHotelDto>? {
        return hotelSearchService.similarHotel(hotelId,language, pageOptions)
    }
    fun getDetailmyHotel(placeId: String, sessionToken : String?, language: String?): MyHotelDto? {
        return hotelDetailService.getDetailMy(placeId, sessionToken,language?:Constants.DEFAULT_LANGUAGE)
    }
    fun getAutoCompleteMy(filters: QueryFilter): AutocompleteResponse? {
        return hotelAutoCompleteService.getAllhotels(filters)
    }
    fun getMySearchHotel(filters: QueryFilter, pageOptions: GraphQLPage): Page<MyHotelDto>? {
        return hotelSearchService.googleSearchhotels(filters, pageOptions)
    }
    fun getRegionAutocomplete(filters: QueryFilter): AutocompleteResponse? {
        return regionAutoCompleteService.getAllRegions(filters,"(regions)")
    }
    fun getSuggestions(location: LocationFilter): SuggestionDto {
        return exploreService.getSuggestions(location)
    }
    fun newSuggestionsAvailable(): Boolean {
        return exploreService.newSuggestionsAvailable()
    }
    fun getTopTrending(location: LocationFilter, pageOptions: GraphQLPage): Page<MyHotelDto>? {
        return exploreService.getTopTrending(location,pageOptions)
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun getTrendingFromFriends(location: LocationFilter, pageOptions: GraphQLPage): Page<MyHotelDto>? {
        return exploreService.getFriendsTrending(location,pageOptions)
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun getFriendsHighlight(language: String?, pageOptions: GraphQLPage): Page<NewsFeeds>? {
        return newsfeedService.getFriendsHighlight(language?:Constants.DEFAULT_LANGUAGE, pageOptions)
    }
    fun getPopularUserHighlight(language: String?, pageOptions: GraphQLPage): Page<NewsFeeds>? {
        return newsfeedService.getPopularUserHighlight(language?:Constants.DEFAULT_LANGUAGE, pageOptions)
    }
    fun getFavoriteHotels(filter: UserHotelFilter, pageOptions: GraphQLPage): Page<UserAddedHotel>? {
        return favoriteService.getFavoriteHotels(filter, pageOptions)
    }
    fun getWishListhotels(filter: UserHotelFilter, pageOptions: GraphQLPage): Page<UserAddedHotel>? {
        return wishListService.getWishListHotel(filter,pageOptions)
    }
    fun getLocationsOfFavoritesHotel(userId: Long?, language: String?, pageOptions: GraphQLPage): Page<PlaceHotelDto>? {
        return favoriteService.getLocationsOfFavoritesHotel(userId, language,pageOptions)
    }
    fun getLocationsOfWishlistHotel(userId: Long?, language: String?, pageOptions: GraphQLPage): Page<PlaceHotelDto>? {
        return wishListService.getLocationsOfWishlistHotel(userId, language, pageOptions)
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun getQualities(): List<Quality>? {
        return qualityService.getQualities()
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun getQualityTypes(): List<QualityType>? {
        return qualityService.getQualityTypes()
    }
    fun getDetailRegion(placeId: String, sessionToken : String?, language: String?): RegionDto? {
        return regionDetailService.getDetailRegion(placeId, sessionToken,language?:Constants.DEFAULT_LANGUAGE)
    }
}