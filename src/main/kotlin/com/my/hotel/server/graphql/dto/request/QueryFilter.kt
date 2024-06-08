package com.my.hotel.server.graphql.dto.request

data class QueryFilter(
    val userQuery: String?=null,
    val latitude: Double?=null,
    val longitude: Double?=null,
    var language: String?=null,
    var sessionToken: String?=null,
)
