package com.my.hotel.server.data.model

import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "status_history")
data class StatusHistory(
    @ManyToOne
    var user: User,
    @ManyToOne
    var from: Status?,
    @ManyToOne
    var to: Status?,
    var createdAt: LocalDate?,
    var updatedAt: LocalDate?,
    @Id
    @GeneratedValue
    var id: Long? = null,
)