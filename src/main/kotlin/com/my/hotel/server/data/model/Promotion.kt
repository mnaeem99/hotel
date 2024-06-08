package com.my.hotel.server.data.model

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "promotion")
data class Promotion(
    var title: String?,
    var titleColor: String?,
    var subTitle: String?,
    var subTitleColor: String?,
    var buttonText: String?,
    var buttonColor: String?,
    var budget: Int?,
    var duration: Int?,
    var showLogo: Boolean?,
    @ManyToOne
    var cover: Image?,
    var geolat: Float?,
    var geolong: Float?,
    var radius: Float?,
    var region: String?,
    var active: Boolean?,
    @ManyToOne
    var hotel: MyHotel,
    @ManyToMany
    @JoinColumn(name = "promotion_id")
    var targetAudience: List<TargetAudience>?,
    var createdAt: LocalDateTime?,
    var modifiedAt: LocalDateTime?,
    @Id
    @GeneratedValue
    var id: Long? = null,
)
