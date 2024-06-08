package com.my.hotel.server.service.wishlist

import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.request.UserHotelFilter
import com.my.hotel.server.graphql.dto.request.WishListHotel
import com.my.hotel.server.graphql.dto.response.MyHotelDto
import org.springframework.data.domain.Page

interface IWishListService {
    fun getWishListHotel(filter: UserHotelFilter, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.UserAddedHotel>?
    fun getLocationsOfWishlistHotel(userId: Long?, language: String?, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.PlaceHotelDto>?
    fun getUsersWhoAddedhotelToWishlists(hotelId: Long, language: String?, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.UserDto>?
    fun addToWishlist(input: WishListHotel): Boolean
    fun isOnWishlist(hotelDto: MyHotelDto): Boolean
}