package com.my.hotel.server.data.model

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "confirmation_code")
data class ConfirmationCode(
    var code: Int,
    var expiry: LocalDateTime,

    @GeneratedValue
    @Id
    var id: Long? = null,

    @OneToOne
    @JoinColumn(name = "user_auth_id", referencedColumnName = "id")
    var userAuth: UserAuthentication? = null,
) {
}
