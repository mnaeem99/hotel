package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.UserAuthentication
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface UserAuthenticationRepository : JpaRepository<UserAuthentication, Long>, JpaSpecificationExecutor<UserAuthentication> {

    @Transactional
    @Modifying
    @Query("delete from UserAuthentication u where u.id = :id")
    fun deleteByAuthId(id: Long): Int
    @Query(value = "SELECT auth FROM UserAuthentication auth WHERE auth.type = 'EMAIL' AND auth.email = ?1")
    fun findByEmail(email: String) : UserAuthentication?
    @Query(value = "SELECT auth FROM UserAuthentication auth WHERE auth.type = 'PHONE' AND auth.phone = ?1")
    fun findByPhone(phone: String) : UserAuthentication?

}