package com.my.hotel.server.graphql.dto.response

import com.my.hotel.server.data.model.Image
import com.my.hotel.server.data.model.User
import java.time.LocalDate

interface IUserDto {
    var id: Long?
    var firstName: String?
    var lastName: String?
    var nickName: String?
    var bio: String?
    var photo: Image?
    var language: String?
    var countryName: String?
    var countryCode: String?
    var countryPicture: Image?
    var countryFlag: Image?
    var countryId: Long?
    var private: Boolean?
    var chef: Boolean?
    var blocked: Boolean?
    var userType: User.UserType?
    var dob: LocalDate?
}