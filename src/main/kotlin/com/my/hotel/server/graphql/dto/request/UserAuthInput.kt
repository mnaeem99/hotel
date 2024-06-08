package com.my.hotel.server.graphql.dto.request

import com.my.hotel.server.data.model.UserAuthentication
import javax.persistence.EnumType
import javax.persistence.Enumerated

data class UserAuthInput(
    @Enumerated(EnumType.STRING)
    var type: UserAuthentication.Type,
    var email : String? = null,
    var phone : String? = null,
    var password : String? = null,
    var googleId : String? = null,
    var appleId : String? = null,
    var facebookId : String? = null,
    var verified : Boolean? = null,
)