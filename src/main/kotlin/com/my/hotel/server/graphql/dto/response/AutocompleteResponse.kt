package com.my.hotel.server.graphql.dto.response

import com.my.hotel.server.data.model.AutoCompleteHotel

data class AutocompleteResponse(
    val autoCompleteHotels: List<AutoCompleteHotel>?,
    val sessionToken: String?
)