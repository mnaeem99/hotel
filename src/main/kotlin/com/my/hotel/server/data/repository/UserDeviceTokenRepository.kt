package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.User
import com.my.hotel.server.data.model.UserDeviceToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface UserDeviceTokenRepository : JpaRepository<UserDeviceToken, Long>, JpaSpecificationExecutor<UserDeviceToken> {
    @Modifying
    @Transactional
    fun deleteByUser(user: User)

    fun findByUser(user: User) : List<UserDeviceToken>?
    fun findByDeviceToken(deviceToken: String) : UserDeviceToken?
    fun findByUserAndDeviceToken(user: User, deviceToken: String) : UserDeviceToken?
}