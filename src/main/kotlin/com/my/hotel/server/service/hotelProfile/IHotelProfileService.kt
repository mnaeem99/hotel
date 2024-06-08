package com.my.hotel.server.service.hotelProfile

import com.my.hotel.server.graphql.GraphQLPage
import org.springframework.data.domain.Page

interface IHotelProfileService {
    fun addHotel(input: com.my.hotel.server.graphql.dto.request.HotelInput): com.my.hotel.server.graphql.dto.response.MyHotelDto?
    fun updateHotel(input: com.my.hotel.server.graphql.dto.request.UpdateHotel): com.my.hotel.server.graphql.dto.response.MyHotelDto?
    fun gethotels(input: com.my.hotel.server.graphql.dto.request.AdminHotelFilter, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.MyHotelDto>?
    fun deleteHotel(hotelId: Long): Boolean?
    fun hotelVerificationAppointment(input: com.my.hotel.server.graphql.dto.request.HotelVerificationAppointmentInput): com.my.hotel.server.graphql.dto.response.HotelVerificationAppointmentDto?
    fun gethotelVerificationAppointments(language: String?, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.HotelVerificationAppointmentDto>?
    fun gethotelInner(hotelId: Long, language: String?): com.my.hotel.server.graphql.dto.response.MyHotelDto?
    fun getLanguages(hotelId: Long): List<String>?
}