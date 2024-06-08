package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.ConfirmationCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ConfirmationCodeRepository : JpaRepository<ConfirmationCode, Long>, JpaSpecificationExecutor<ConfirmationCode> {

    @Query(
        value = "SELECT cc FROM ConfirmationCode cc WHERE cc.userAuth.type = 'EMAIL' " +
                "AND cc.userAuth.email = ?1",
        )
    fun findByEmail(email: String) : ConfirmationCode?

    @Query(
        value = "SELECT cc FROM ConfirmationCode cc WHERE cc.userAuth.type = 'PHONE' " +
                "AND cc.userAuth.phone = ?1",
        )
    fun findByPhone(phone: String) : ConfirmationCode?

    @Query("SELECT cc FROM ConfirmationCode cc WHERE cc.userAuth.id = ?1")
    fun findByUserAuth(userAuthId: Long): List<ConfirmationCode>?


}
