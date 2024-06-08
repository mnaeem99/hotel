package com.my.hotel.server.data.model

import java.net.URL
import javax.persistence.*

@Entity
@Table(name = "language")
data class Language(
    @Id
    var code: String,
    var imageUrl: URL?=null,
    var name: String?  = null,
)
