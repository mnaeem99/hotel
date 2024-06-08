package com.my.hotel.server.data.model

import javax.persistence.*

@Entity
@Table(name = "country_address_config")
data class CountryAddressConfig(
        @ManyToOne
        var country: Country,
        var type: String,
        var priority: Int,
        @Enumerated(EnumType.STRING)
        var level: AddressLevel,
        @Id
        @GeneratedValue
        var id: Long? = null
){
        enum class AddressLevel{
                LEVEL1, LEVEL2
        }
}