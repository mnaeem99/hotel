package com.my.hotel.server.service.explore

import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.request.LocationFilter
import com.my.hotel.server.graphql.dto.request.QueryFilter
import com.my.hotel.server.graphql.dto.response.UserDto
import com.my.hotel.server.service.event.dto.Event
import org.springframework.data.domain.Page

interface IExploreService {
    fun getSuggestions(location: LocationFilter): com.my.hotel.server.graphql.dto.response.SuggestionDto
    fun newSuggestionsAvailable(): Boolean
    fun getTopTrending(location: LocationFilter, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.MyHotelDto>?
    fun getFriendsTrending(location: LocationFilter, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.MyHotelDto>?
    fun searchUser(filters: QueryFilter, pageOptions: GraphQLPage): Page<UserDto>?
    fun executeSuggestions(event: Event)
}