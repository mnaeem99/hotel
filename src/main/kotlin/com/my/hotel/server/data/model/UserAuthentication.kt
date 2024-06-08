package com.my.hotel.server.data.model

import javax.persistence.*

@Entity
@Table(name = "user_auths")
data class UserAuthentication(
    @Enumerated(EnumType.STRING)
    var type: Type,
    var email: String? = null,
    var phone: String? = null,
    var password: String? = null,
    var googleId: String? = null,
    var appleId: String? = null,
    var facebookId: String? = null,
    var verified: Boolean? = null,

    @OneToOne(mappedBy="userAuth", fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    var confirmationCode: ConfirmationCode? = null,

    @ManyToOne
    var user: User?,

    @Id
    @GeneratedValue
    var id: Long? = null,
) {
    enum class Type {
        EMAIL,
        PHONE,
        GOOGLE,
        APPLE,
        FACEBOOK
    }
}