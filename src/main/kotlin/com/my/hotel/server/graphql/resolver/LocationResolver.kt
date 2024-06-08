package com.my.hotel.server.graphql.resolver
import graphql.kickstart.tools.GraphQLResolver
import org.postgis.Point
import org.springframework.stereotype.Component

@Component
class LocationResolver(
) : GraphQLResolver<Point> {

    fun getLatitude(point: Point): Double {
        return point.getX()
    }

    fun getLongitude(point: Point): Double {
        return point.getX()
    }

}
