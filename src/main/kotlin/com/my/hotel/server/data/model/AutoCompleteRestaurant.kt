package com.my.hotel.server.data.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "google_auto_complete_hotel")
data class AutoCompleteHotel (
    var description: String? = null,
    var reference: String? = null,
    @JsonProperty("main_text")
    var name: String? = null,
    @JsonProperty("secondary_text")
    var address: String? = null,
    var types: ArrayList<String>? = null,
    var expiryDate: LocalDateTime?=null,
    @Id
    @Column(name = "id", nullable = false)
    var placeId: String? = null,
)