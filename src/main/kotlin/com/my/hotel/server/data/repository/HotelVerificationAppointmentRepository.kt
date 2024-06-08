package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.MyHotel
import com.my.hotel.server.data.model.HotelVerificationAppointment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional


@Repository
interface HotelVerificationAppointmentRepository : JpaRepository<HotelVerificationAppointment, Long>, JpaSpecificationExecutor<HotelVerificationAppointment>{
    fun findByHotel(hotel: MyHotel) : HotelVerificationAppointment?

    @Modifying
    @Transactional
    fun deleteByHotel(hotel: MyHotel)
}