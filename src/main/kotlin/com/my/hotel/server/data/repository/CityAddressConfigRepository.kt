package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.City
import com.my.hotel.server.data.model.CityAddressConfig
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CityAddressConfigRepository : JpaRepository<CityAddressConfig, Long>, JpaSpecificationExecutor<CityAddressConfig> {
    fun findByTypeAndCity(type: String, city: City): CityAddressConfig?
    @Query("SELECT config FROM CityAddressConfig config WHERE config.city.id = :cityId ")
    fun findByCity(cityId: Long): List<CityAddressConfig>
}