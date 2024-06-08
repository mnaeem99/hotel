package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.GoogleHotelAddress
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface GoogleHotelAddressRepository : JpaRepository<GoogleHotelAddress, String>, JpaSpecificationExecutor<GoogleHotelAddress>{
}