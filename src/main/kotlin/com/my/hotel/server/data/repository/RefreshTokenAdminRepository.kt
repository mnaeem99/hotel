package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.RefreshTokenAdmin
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface RefreshTokenAdminRepository : JpaRepository<RefreshTokenAdmin, Long>, JpaSpecificationExecutor<RefreshTokenAdmin> {
    fun findByToken(token: String) : RefreshTokenAdmin
}