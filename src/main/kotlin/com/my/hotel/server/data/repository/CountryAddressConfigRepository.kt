package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.Country
import com.my.hotel.server.data.model.CountryAddressConfig
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CountryAddressConfigRepository : JpaRepository<CountryAddressConfig, Long>, JpaSpecificationExecutor<CountryAddressConfig> {
    fun findByTypeAndCountry(type: String, country: Country): CountryAddressConfig?
    @Query("SELECT config FROM CountryAddressConfig config WHERE config.country.id = :countryId ")
    fun findByCountry(countryId: Long): List<CountryAddressConfig>
}