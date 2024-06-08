package com.my.hotel.server.graphql.dto.response

import com.my.hotel.server.data.model.Image

interface Chef {
    var id: Long?
    var firstName: String?
    var lastName: String?
    var nickName: String?
    var bio: String?
    var photo: Image?
}