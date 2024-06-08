package com.my.hotel.server.data.model

import java.net.URL
import javax.persistence.*

@Entity
@Table(name = "images")
data class Image(
    var imageUrl: URL?=null,
    var thumbnailUrl: URL? =null,
    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
)