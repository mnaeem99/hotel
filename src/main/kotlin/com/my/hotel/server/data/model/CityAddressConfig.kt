package com.my.hotel.server.data.model

import javax.persistence.*

@Entity
@Table(name = "city_address_config")
data class CityAddressConfig(
        @ManyToOne
        var city: City,
        var type: String,
        var priority: Int,
        @Id
        @GeneratedValue
        var id: Long? = null
)