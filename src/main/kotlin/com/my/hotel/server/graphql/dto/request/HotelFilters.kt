package com.my.hotel.server.graphql.dto.request

data class HotelFilters(
    val name: String ? = null,
    val countries: List<String>? = null,
    val favoriteOfUser: Long? = null,
    val wishlistOfUser: Long? = null
)