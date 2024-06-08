package com.my.hotel.server.graphql.dto.request

data class SearchUserFilter(
    val userId: Long,
    val language: String? = null,
    val keyword: String? = null,
)