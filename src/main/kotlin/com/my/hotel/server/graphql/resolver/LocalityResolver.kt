package com.my.hotel.server.graphql.resolver


import com.my.hotel.server.data.repository.FavoriteRepository
import com.my.hotel.server.graphql.dto.response.LocalityDto
import com.my.hotel.server.graphql.security.Unsecured
import graphql.kickstart.tools.GraphQLResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDateTime


@Component
class LocalityResolver @Autowired constructor(
    val favoriteRepository: FavoriteRepository
) : GraphQLResolver<LocalityDto> {
    @Unsecured
    fun latesthotelAddedAt(localityDto: LocalityDto): LocalDateTime? {
        return favoriteRepository.findLatesthotelAddedAt(localityDto.id!!)
    }
}
