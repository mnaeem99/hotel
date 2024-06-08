package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.RefreshToken
import com.my.hotel.server.data.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long>, JpaSpecificationExecutor<RefreshToken> {
    fun findByToken(token: String) : RefreshToken
    @Modifying
    @Transactional
    fun deleteByUser(user: User)
}