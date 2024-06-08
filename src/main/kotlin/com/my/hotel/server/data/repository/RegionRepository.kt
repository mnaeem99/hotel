package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.Country
import com.my.hotel.server.data.model.Region

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying

import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface RegionRepository : JpaRepository<Region, String>, JpaSpecificationExecutor<Region>{

    fun findByPlaceId(placeId: String?) : Region?

    @Modifying
    @Transactional
    fun deleteByCountry(country: Country)

}
