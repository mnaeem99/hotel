package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.AutoCompleteHotel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface AutoCompleteHotelRepository : JpaRepository<AutoCompleteHotel, String>, JpaSpecificationExecutor<AutoCompleteHotel>