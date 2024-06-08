package com.my.hotel.server.service.newsFeed

import com.my.hotel.server.graphql.GraphQLPage
import org.springframework.data.domain.Page

interface INewsfeedService {
    fun getPopularUserHighlight(language: String, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.NewsFeeds>?
    fun getFriendsHighlight(language: String, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.NewsFeeds>?
}