package com.my.hotel.server.commons

object Constants {
    const val PHONE_ALREADY_IN_USE = "This phone number is already in use."
    const val EMAIL_ALREADY_IN_USE = "This email is already in use."
    const val NICKNAME_ALREADY_IN_USE = "This nickname is already in use."
    val CONFIRMATION_EMAIL = listOf(
        "Hello,\n\n Your confirmation code is ",
        ". The confirmation code will be valid for 60 minutes." +
        "If you did not initiate this operation, you can ignore this email.\n\n" +
        "My Team\n" +
        "Automated message. Please do not reply.", "[My] Email Confirmation"
    )
    const val CONFIRMATION_EMAIL_SUBJECT = "[My] Email Confirmation"
    const val INVALID_VERIFICATION_CODE: String = "Invalid verification code"
    const val VERIFICATION_PHONE = " is your verification code from My"
    const val RECORD_NOT_FOUND = "Record was not found"
    const val PROFILE_PICTURE_MAX_SIZE_EXCEEDS = "Size of profile picture should not be more than "
    const val INVALID_LOCATION = "Invalid Latitude or Longitude"
    const val ALREADY_GET_SUGGESTIONS = "New suggestions are available at "
    const val REQUEST_ALREADY_IN_USE = "Request is already in progress."
    const val hotel_ALREADY_EXIST = "hotel is already exist."
    const val COUNTRY_ALREADY_EXIST = "Country is already exist."
    const val CITY_ALREADY_EXIST = "City is already exist."
    const val LOCALITY_ALREADY_EXIST = "Locality is already exist."
    const val ADDRESS_TYPE_ALREADY_EXIST = "Address Type is already exist."
    const val COUNTRY_ALREADY_PRESENT = "We were unable to delete this country because a country is used in "
    const val CITY_ALREADY_PRESENT = "We were unable to delete this city because a city is used in "
    const val LOCALITY_ALREADY_PRESENT = "We were unable to delete this locality because a locality is used in "
    const val CHEF_ALREADY = "Already a chef."
    const val QUALITY_TYPE_ALREADY_PRESENT = "We were unable to delete quality type because a quality is present for this type"
    const val GOOGLE_API_BASE_URL = "https://maps.googleapis.com/maps/api/place"
    const val GOOGLE_API_DETAIL_URL = "%s/details/json"
    const val GOOGLE_API_AUTO_URL = "%s/autocomplete/json"
    const val GOOGLE_API_TEXT_SEARCH_URL = "%s/textsearch/json"
    const val GOOGLE_API_NEARBY_SEARCH_URL = "%s/nearbysearch/json"
    const val DETAIL_REGION_FIELDS = "address_components,formatted_address,geometry,name,photos,place_id"
    const val NEW_DETAIL_hotel_FIELDS = "address_components,adr_address,business_status,formatted_address,formatted_phone_number,international_phone_number,geometry,name,photos,place_id,price_level,vicinity,url"
    const val UPDATE_DETAIL_hotel_FIELDS = "address_components,adr_address,business_status,formatted_address,geometry,name,place_id,vicinity,url"
    const val DEFAULT_LANGUAGE = "en"
    const val AUTHENTICATION_ERROR = "Invalid credentials"
    const val INCORRECT_PASSWORD = "Current password is incorrect"
    const val GRAPH_API_URL = "https://graph.facebook.com"
    const val S3_BUCKET_ERROR = "Exception while retrieving s3 buckets: "
}