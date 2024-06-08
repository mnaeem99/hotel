package com.my.hotel.server.data.model

import org.locationtech.jts.geom.Point
import javax.persistence.*

@Entity
@Table(name = "my_region")
data class Region(
    var geolat: Float?=null,
    var geolong: Float?=null,
    @OneToOne(cascade = [CascadeType.MERGE])
    @JoinColumn(name = "photo_id")
    var photo: Image?  = null,
    var point: Point?=null,
    @ManyToOne
    var country: Country?=null,
    @Id
    @Column(name = "id", nullable = false)
    var placeId: String? = null,
)