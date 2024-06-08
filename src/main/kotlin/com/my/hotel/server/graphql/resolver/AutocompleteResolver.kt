package com.my.hotel.server.graphql.resolver

import com.my.hotel.server.data.model.AutoCompleteHotel
import com.my.hotel.server.data.model.Image
import com.my.hotel.server.data.repository.GoogleHotelRepository
import com.my.hotel.server.data.repository.MyHotelRepository
import graphql.kickstart.tools.GraphQLResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component


@Component
class AutocompleteResolver @Autowired constructor(
    val myHotelRepository: MyHotelRepository,
    val googleHotelRepository: GoogleHotelRepository,
) : GraphQLResolver<AutoCompleteHotel> {
    fun getPhoto(autoCompleteHotel: AutoCompleteHotel): Image? {
        val hotel = myHotelRepository.findByPlaceId(autoCompleteHotel.placeId)
        if (hotel != null)
            return hotel.photo
        val googlehotel = googleHotelRepository.findByIdOrNull(autoCompleteHotel.placeId) ?: return null
        return googlehotel.photo
    }
}
