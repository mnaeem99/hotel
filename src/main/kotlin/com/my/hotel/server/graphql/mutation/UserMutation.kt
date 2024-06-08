package com.my.hotel.server.graphql.mutation

import com.my.hotel.server.data.model.NotificationSetting
import com.my.hotel.server.data.model.User
import com.my.hotel.server.graphql.dto.request.Location
import com.my.hotel.server.graphql.dto.response.FollowStatus
import com.my.hotel.server.graphql.dto.response.NotificationDto
import com.my.hotel.server.graphql.dto.response.UserProfile
import com.my.hotel.server.graphql.security.Unsecured
import com.my.hotel.server.service.follow.FollowService
import com.my.hotel.server.service.notification.NotificationService
import com.my.hotel.server.service.user.IUserService
import com.my.hotel.server.service.user.dto.UpdateProfile
import graphql.kickstart.tools.GraphQLMutationResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestBody
import java.util.*
import javax.validation.constraints.Pattern

@Component
@Validated
class UserMutation @Autowired constructor(
    val userService: IUserService,
    val followService: FollowService,
    val notificationService: NotificationService
): GraphQLMutationResolver {

    /**
     * Set Device Token for the user.
     * @param token device token
     */
    @PreAuthorize("hasAnyAuthority('USER')")
    fun setDeviceToken(token: String): Boolean {
        return userService.setDeviceToken(token)
    }
    /**
     * Set Device Token for the user.
     * @param token device token
     */
    @PreAuthorize("hasAnyAuthority('USER')")
    fun deleteDeviceToken(token: String): Boolean {
        return userService.deleteDeviceToken(token)
    }

    /**
     * Creates user using email and sends out confirmation code.
     * @param email email of the new user
     */
    @Unsecured
    fun requestEmailVerificationCode(
        @Pattern(regexp = "(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])", message = "Email Address should be valid")
        email: String): Boolean {
        return userService.requestEmailVerificationCode(email)
    }

    @Unsecured
    fun verifyEmailVerificationCode(email: String, code: Int, deviceID: String?): HashMap<String, String>? {
        return userService.verifyEmailVerificationCode(email, code, deviceID)
    }

    @Unsecured
    fun requestPhoneVerificationCode(phone: String): Boolean {
        return userService.requestPhoneVerificationCode(phone)
    }

    @Unsecured
    fun verifyPhoneVerificationCode(phone: String, code: Int, deviceID: String?): HashMap<String, String>? {
        return userService.verifyPhoneVerificationCode(phone, code, deviceID)
    }

    @PreAuthorize("hasAnyAuthority('USER')")
    fun updateProfile(@ModelAttribute @RequestBody input: UpdateProfile): UserProfile {
        val user = userService.updateProfile(input)
        return UserProfile(user!!)
    }

    fun updateLocation(location: Location, timezoneId: String?): Boolean {
        return userService.updateLocation(location, timezoneId)
    }

    // ugly workaround to use Date extended scalar type in input
    fun dummyupdateProfile(dob: Date): User? {
        return null
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun updatePassword(password: String): Boolean {
        return userService.updatePassword(password)
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun changePassword(currentPassword:String, newPassword: String): Boolean {
        return userService.changePassword(currentPassword, newPassword)
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun deleteProfile(): Boolean {
        return userService.deleteUserProfile()
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun updateNotificationSettings(@ModelAttribute @RequestBody input: com.my.hotel.server.graphql.dto.request.UpdateNotificationSetting): NotificationSetting? {
        return userService.updateNotificationSettings(input)
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun updateAccount(private: Boolean): Boolean? {
        return userService.updateAccount(private)
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun updateEmail(@Pattern(regexp = "(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])", message = "Email Address should be valid")
                    email: String): Boolean{
        return userService.updateEmail(email)
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun updatePhone(phone: String): Boolean {
        return userService.updatePhone(phone)
    }
    @Unsecured
    fun requestEmailResetPassword(
        @Pattern(regexp = "(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])", message = "Email Address should be valid")
        email: String): Boolean {
        return userService.requestResetPasswordCodeByEmail(email)
    }
    @Unsecured
    fun requestPhoneResetPassword(phone: String): Boolean {
        return userService.requestResetPasswordCodeByPhone(phone)
    }

    // Follows
    @PreAuthorize("hasAnyAuthority('USER')")
    fun followRequest(userId: Long): FollowStatus {
        return followService.followRequest(userId)
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun unfollowRequest(userId: Long): FollowStatus {
        return followService.unfollowRequest(userId)
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun cancelFollowRequest(userId: Long): FollowStatus {
        return followService.cancelFollowRequest(userId)
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun acceptRequest(userId: Long): FollowStatus {
        return followService.acceptRequest(userId)
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun rejectRequest(userId: Long): FollowStatus {
        return followService.rejectRequest(userId)
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun blockRequest(userId: Long): Boolean {
        return followService.blockRequest(userId)
    }
    @PreAuthorize("hasAnyAuthority('USER')")
    fun unBlockRequest(userId: Long): Boolean {
        return followService.unBlockRequest(userId)
    }

    //Chef
    @PreAuthorize("hasAnyAuthority('USER')")
    fun requestChefVerification(): Boolean {
        return userService.requestChefVerification()
    }

    @PreAuthorize("hasAnyAuthority('USER')")
    fun readNotification(notificationId: Long): NotificationDto {
        return notificationService.readNotification(notificationId)
    }
}