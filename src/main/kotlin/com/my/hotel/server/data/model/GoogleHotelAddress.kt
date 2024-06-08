package com.my.hotel.server.data.model

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "google_hotel_address")
data class GoogleHotelAddress(

    @Column(name="address_components", columnDefinition="TEXT")
    var addressComponents: String? = null,

    @Column(name="adr_address", columnDefinition="TEXT")
    var adrAddress: String? = null,

    var createdAt: LocalDateTime? = null,

    @Id
    var placeId: String,

)
