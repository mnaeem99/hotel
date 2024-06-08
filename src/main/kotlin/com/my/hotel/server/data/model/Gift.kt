package com.my.hotel.server.data.model

import javax.persistence.*

@Entity
@Table(name = "gift")
data class Gift(
    var name: String,
    @OneToOne
    var picture: Image?  = null,
    var points: Int,
    var otherInfo: String,
    @ManyToOne
    var hotel: MyHotel,
    @Id
    @GeneratedValue
    var id: Long? = null,
)
