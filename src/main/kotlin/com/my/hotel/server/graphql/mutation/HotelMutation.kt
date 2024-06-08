package com.my.hotel.server.graphql.mutation

import com.my.hotel.server.graphql.dto.request.FavoriteHotel
import com.my.hotel.server.graphql.dto.request.WishListHotel
import com.my.hotel.server.service.favorites.FavoriteService
import com.my.hotel.server.service.wishlist.WishListService
import graphql.kickstart.tools.GraphQLMutationResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component

@Component
class HotelMutation @Autowired constructor(
    val favoriteService: FavoriteService,
    val wishListService: WishListService,
    ): GraphQLMutationResolver {

    @PreAuthorize("hasAnyAuthority('USER')")
    fun addToFavorite(input: FavoriteHotel): Boolean {
        return favoriteService.addToFavorite(input)
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun addhotelsToFavorite(input: List<FavoriteHotel>): Boolean {
        return favoriteService.addhotelsToFavorite(input)
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun addToWishlist(input: WishListHotel): Boolean {
        return wishListService.addToWishlist(input)
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun addPriceRange(input: com.my.hotel.server.graphql.dto.request.PriceRange): Boolean {
        return favoriteService.addPriceRange(input)
    }
}