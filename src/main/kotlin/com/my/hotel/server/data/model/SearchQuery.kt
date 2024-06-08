package com.my.hotel.server.data.model

import org.locationtech.jts.geom.Point
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "query_search")
data class SearchQuery(
    var keyword: String?=null,
    var radius: Double?=null,
    var type: String?=null,
    var language: String?=null,
    @ManyToOne
    var user: User?=null,
    @ManyToMany
    @JoinColumn(name = "search_query_id")
    var response: List<GoogleHotel>? = null,
    var expiryDate: LocalDateTime?=null,
    var point: Point?=null,
    @Id
    @GeneratedValue
    var id: Long? = null,
)
