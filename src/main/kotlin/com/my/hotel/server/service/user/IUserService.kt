package com.my.hotel.server.service.user

import com.my.hotel.server.data.model.NotificationSetting
import com.my.hotel.server.data.model.User
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.request.Location
import com.my.hotel.server.graphql.dto.response.UserProfileAdmin
import com.my.hotel.server.service.user.dto.UpdateProfile
import org.springframework.data.domain.Page

interface IUserService {
    fun requestEmailVerificationCode(email: String): Boolean
    fun verifyEmailVerificationCode(email: String, code: Int, deviceID: String?): HashMap<String, String>?
    fun requestPhoneVerificationCode(phone: String): Boolean
    fun verifyPhoneVerificationCode(phone: String, code: Int, deviceID: String?): HashMap<String, String>?
    fun updateProfile(fields: UpdateProfile): com.my.hotel.server.graphql.dto.response.UserDto?
    fun getLoggedInUser(): User?
    fun updatePassword(password: String): Boolean
    fun deleteUserProfile(): Boolean
    fun updateAccount(private: Boolean): Boolean?
    fun updateEmail(email: String): Boolean
    fun updatePhone(phone: String): Boolean
    fun updateNotificationSettings(fields: com.my.hotel.server.graphql.dto.request.UpdateNotificationSetting): NotificationSetting?
    fun requestResetPasswordCodeByEmail(email: String): Boolean
    fun requestResetPasswordCodeByPhone(phone: String): Boolean
    fun requestChefVerification(): Boolean
    fun getUsers(input: com.my.hotel.server.graphql.dto.request.AdminUserFilter, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.UserDto>?
    fun addUser(input: com.my.hotel.server.graphql.dto.request.UserInput): com.my.hotel.server.graphql.dto.response.UserDto?
    fun updateUser(input: com.my.hotel.server.graphql.dto.request.UpdateUser): com.my.hotel.server.graphql.dto.response.UserDto?
    fun deleteUser(id: Long): Boolean
    fun getUserAdmin(id: Long, language: String?): UserProfileAdmin
    fun searchUserByNickname(nickName: String, language: String?): com.my.hotel.server.graphql.dto.response.UserDto?
    fun blockUserAdmin(userId: Long, block: Boolean): Boolean
    fun getProfile(language: String?): com.my.hotel.server.graphql.dto.response.UserProfile
    fun getUser(id: Long, language: String?): com.my.hotel.server.graphql.dto.response.UserDto?
    fun setDeviceToken(token: String): Boolean
    fun deleteDeviceToken(token: String): Boolean
    fun updateLocation(location: Location, timezoneId: String?): Boolean
    fun changePassword(currentPassword: String, newPassword: String): Boolean
}