package com.my.hotel.server.data.model

import org.locationtech.jts.geom.Point
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "my_hotels")
data class MyHotel(
    var phone : String? = null,
    @ManyToOne
    var country: Country?=null,
    @ManyToOne
    var city: City?=null,
    @ManyToOne
    var locality: Locality?=null,
    var geolat: Float?=null,
    var geolong: Float?=null,
    @ManyToOne
    var googlePriceLevel: HotelPriceLevel?=null,
    @ManyToOne
    var hotelPriceLevel: HotelPriceLevel?=null,
    @OneToMany
    @JoinColumn(name="hotel_id")
    var photoList: List<Image>?  = ArrayList(),
    @OneToOne(cascade = [CascadeType.MERGE])
    @JoinColumn(name = "photo_id")
    var photo: Image?  = null,
    var placeId: String?=null,
    var googleMapUrl: String? = null,
    var point: Point?=null,
    var createdAt: LocalDateTime?=null,
    var expiryDate: LocalDateTime?=null,
    @Enumerated(EnumType.STRING)
    var status: BusinessStatus?=null,
    @Id
    @GeneratedValue
    var id: Long? = null,
){
    enum class BusinessStatus{
        OPERATIONAL, CLOSED_TEMPORARILY, CLOSED_PERMANENTLY
    }
}