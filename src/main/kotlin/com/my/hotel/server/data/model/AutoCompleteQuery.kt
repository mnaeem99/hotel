package com.my.hotel.server.data.model

import com.my.hotel.server.graphql.dto.response.AutocompleteResponse
import org.locationtech.jts.geom.Point
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "query_autocomplete")
data class AutoCompleteQuery(
    var userQuery: String?=null,
    var radius: Double?=null,
    var components: String?=null,
    var language: String?=null,
    var offSet: String?=null,
    var origin: String?=null,
    var sessionToken: String?=null,
    var types: String?=null,
    @ManyToOne
    var user: User?=null,
    @ManyToMany(cascade = [CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST])
    @JoinColumn(name = "auto_complete_query_id")
    var response: List<AutoCompleteHotel>? = null,
    var expiryDate: LocalDateTime?=null,
    var point: Point?=null,
    @Id
    @GeneratedValue
    var id: Long? = null,
)
