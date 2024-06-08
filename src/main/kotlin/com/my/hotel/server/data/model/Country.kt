package com.my.hotel.server.data.model

import javax.persistence.*

@Entity
@Table(name = "country")
data class Country(
    var code: String,
    @OneToOne
    var picture: Image?  = null,
    @OneToOne
    var flag: Image?  = null,
    @OneToMany
    @JoinColumn(name="country_id")
    var city: List<City>?  = ArrayList(),
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval=true)
    @JoinColumn(name = "country_id")
    val addressConfig: List<CountryAddressConfig> = ArrayList(),
    @Id
    @GeneratedValue
    var id: Long? = null,
)
