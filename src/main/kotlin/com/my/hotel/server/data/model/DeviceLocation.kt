package com.my.hotel.server.data.model

import org.locationtech.jts.geom.Point
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "device_location")
data class DeviceLocation(

    @ManyToOne
    var user: User? = null,
    var point: Point?= null,
    var createdAt: LocalDateTime? = null,
    var modifiedAt: LocalDateTime? = null,

    @Id
    @Column(name = "id", nullable = false)
    val deviceId: String,
)
