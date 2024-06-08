package com.my.hotel.server.service.hotel.dto

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty


data class ResultDTO (
    @JsonProperty("address_components")
    var addressComponents: ArrayList<AddressComponent>? = null,
    @JsonProperty("adr_address")
    var adrAddress: String? = null,
    @JsonProperty("business_status")
    var businessStatus: String? = null,
    @JsonProperty("formatted_address")
    var formattedAddress: String? = null,
    @JsonProperty("formatted_phone_number")
    var formattedPhoneNumber: String? = null,
    @JsonProperty("geometry")
    var geometry: Geometry? = null,
    @JsonProperty("icon")
    var icon: String? = null,
    @JsonProperty("icon_background_color")
    var iconBackgroundColor: String? = null,
    @JsonProperty("icon_mask_base_uri")
    var iconMaskBaseUri: String? = null,
    @JsonProperty("international_phone_number")
    var internationalPhoneNumber: String? = null,
    @JsonProperty("name")
    var name: String? = null,
    @JsonProperty("opening_hours")
    var openingHours: OpeningHours? = null,
    @JsonProperty("permanently_closed")
    var permanentlyClosed:Boolean?=false,
    @JsonProperty("photos")
    var photos: ArrayList<Photo>? = null,
    @JsonProperty("place_id")
    var placeId: String? = null,
    @JsonProperty("plus_code")
    var plusCode: PlusCode? = null,
    @JsonProperty("price_level")
    var priceLevel: Int? = null,
    @JsonProperty("rating")
    var rating: Double?=null,
    @JsonProperty("reference")
    var reference: String? = null,
    @JsonProperty("reviews")
    var reviews: ArrayList<Review>? = null,
    @JsonProperty("scope")
    var scope: String? = null,
    @JsonProperty("types")
    var types: ArrayList<String>? = null,
    @JsonProperty("url")
    var url: String? = null,
    @JsonProperty("user_ratings_total")
    var userRatingsTotal:Long? = null,
    @JsonProperty("utc_offset")
    var utcOffset: Long? = null,
    @JsonProperty("vicinity")
    var vicinity: String? = null,
    @JsonProperty("website")
    var website: String?=null
)

class Location {
    var lat = 0.0
    var lng = 0.0
}
class Viewport {
    var northeast: Location? = null
    var southwest: Location? = null
}
class Geometry {
    var location: Location? = null
    var viewport: Viewport? = null
}
class OpeningHours {
    @JsonProperty("open_now")
    var openNow = false
    @JsonProperty("periods")
    var periods: ArrayList<Period>? = null
    @JsonProperty("weekday_text")
    var weekdayText: ArrayList<String>? = null
}
class Photo {
    @JsonProperty("height")
    var height = 0
    @JsonProperty("html_attributions")
    var htmlAttributions: ArrayList<String>? = null
    @JsonProperty("photo_reference")
    var photoReference: String? = null
    @JsonProperty("width")
    var width = 0
}
class PlusCode {
    @JsonProperty("compound_code")
    var compoundCode: String? = null
    @JsonProperty("global_code")
    var globalCode: String? = null
}
class Review {
    @JsonProperty("author_name")
    var authorName: String? = null
    @JsonProperty("author_url")
    var authorUrl: String? = null
    @JsonProperty("language")
    var language: String? = null
    @JsonProperty("profile_photo_url")
    var profilePhotoUrl: String? = null
    @JsonProperty("rating")
    var rating = 0
    @JsonProperty("relative_time_description")
    var relativeTimeDescription: String? = null
    @JsonProperty("text")
    var text: String? = null
    @JsonProperty("time")
    var time = 0
}
class AddressComponent {
    @JsonAlias("long_name", "longName")
    var longName: String? = null
    @JsonAlias("short_name", "shortName")
    var shortName: String? = null
    @JsonProperty("types")
    var types: ArrayList<String>? = null
}
class PeriodDetail {
    var day = 0
    var time: String? = null
}
class Period {
    var close: PeriodDetail ? = null
    @JsonProperty("open")
    var open: PeriodDetail? = null
}