package com.my.hotel.server.data.model

import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit
import javax.persistence.*

@Entity
data class RefreshTokenAdmin (
    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne
    var admin: Admin? = null,

    @Column(unique = true, nullable = false)
    var token: String? = null,

    @CreationTimestamp
    var createdAt: OffsetDateTime? = null,

    @Column(nullable = false)
    var validityPeriod: Long = TimeUnit.DAYS.toMillis(30)
)