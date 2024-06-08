package com.my.hotel.server.data.model

import javax.persistence.*

@Entity
@Table(name = "city")
data class City(
    @OneToOne
    var picture: Image?  = null,
    @OneToMany
    @JoinColumn(name="city_id")
    var locality: List<Locality>?  = ArrayList(),
    var placeId: String? = null,
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval=true)
    @JoinColumn(name = "city_id")
    val addressConfig: List<CityAddressConfig> = ArrayList(),
    @Id
    @GeneratedValue
    var id: Long? = null,
)
