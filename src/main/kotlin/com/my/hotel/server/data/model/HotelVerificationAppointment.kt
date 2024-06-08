package com.my.hotel.server.data.model

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "hotel_verification_appointment")
data class HotelVerificationAppointment(
    var appointmentDate: LocalDateTime,
    @ManyToOne
    var hotel: MyHotel,
    @Id
    @GeneratedValue
    var id: Long? = null
)