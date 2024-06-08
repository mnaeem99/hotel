package com.my.hotel.server.data.model

import com.my.hotel.server.graphql.dto.response.MyHotelDto
import org.locationtech.jts.geom.Point
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "google_hotel")
data class GoogleHotel(
    var name: String?,
    var address: String?=null,
    var phone : String? = null,
    var geolat: Float?=null,
    var geolong: Float?=null,
    @ManyToOne
    var hotelPriceLevel: HotelPriceLevel?=null,
    @OneToOne(cascade = [CascadeType.MERGE])
    @JoinColumn(name = "photo_id")
    var photo: Image?  = null,
    var point: Point?=null,
    var expiryDate: LocalDateTime?=null,
    @Enumerated(EnumType.STRING)
    var status: MyHotel.BusinessStatus?=null,
    var language: String?=null,
    var createdAt: LocalDateTime?=null,
    var googleMapUrl: String? = null,
    @ManyToOne
    var country: Country?=null,
    @ManyToOne
    var city: City?=null,
    @ManyToOne
    var locality: Locality?=null,
    @OneToMany
    @JoinColumn(name="hotel_id")
    var photoList: List<Image>?  = ArrayList(),
    @Id
    @Column(name = "id", nullable = false)
    var placeId: String? = null,
): Serializable{
    fun tomyHotelDto() = MyHotelDto(
        name = name,
        address = address,
        phone = phone,
        country = null,
        geolat = geolat,
        geolong = geolong,
        hotelPriceLevel = hotelPriceLevel,
        googlePriceLevel = hotelPriceLevel,
        photoList = toPhotoList(),
        photo = photo,
        placeId = placeId,
        expiryDate = expiryDate,
        googleMapUrl = googleMapUrl,
        id = null
    )
    private fun toPhotoList(): List<Image>? {
        if (photo != null)
            return listOf(photo!!)
        return null
    }
}