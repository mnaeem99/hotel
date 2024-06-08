package com.my.hotel.server.graphql.resolver


import com.my.hotel.server.data.model.Quality
import com.my.hotel.server.data.repository.FavoriteRepository
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.security.Unsecured
import graphql.kickstart.tools.GraphQLResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


@Component
class ChefResolver @Autowired constructor(
    val favoriteRepository: FavoriteRepository,

) : GraphQLResolver<com.my.hotel.server.graphql.dto.response.Chef> {
    @Unsecured
    fun getQualities(chef: com.my.hotel.server.graphql.dto.response.Chef): List<Quality>? {
        val graphQPage = GraphQLPage(0,3)
        return favoriteRepository.findChefQualities(chef.id, graphQPage.toPageable())?.content
    }
}
