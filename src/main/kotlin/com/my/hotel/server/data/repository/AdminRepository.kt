package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.Admin
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface AdminRepository : JpaRepository<Admin, Long>, JpaSpecificationExecutor<Admin> {
    @Query("SELECT u FROM Admin u WHERE u.username = ?1")
    fun findByUsername(username: String) : Admin?
}