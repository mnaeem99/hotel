package com.my.hotel.server.graphql.dto.response

import com.my.hotel.server.data.model.User

interface UserRank {
    val user: User?
    val ranking: Int?
}