package com.my.hotel.server.data.model

import javax.persistence.*

@Entity
@Table(name = "qr_code")
data class QRCode(
    var redeemed: Boolean,
    @ManyToOne
    var hotel: MyHotel,
    @ManyToOne
    var order: Order,
    @Id
    @GeneratedValue
    var id: Long? = null,
)
