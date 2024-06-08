package com.my.hotel.server.data.model

import javax.persistence.*

@Entity
@Table(name = "locality")
data class Locality(
    @OneToOne
    var picture: Image?  = null,
    var placeId: String? = null,
    @Id
    @GeneratedValue
    var id: Long? = null,
)
