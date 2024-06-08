package com.my.hotel.server.service.hotel.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class PredictionDTO(
    @JsonProperty("description")
    var description: String? = null,
    @JsonProperty("matched_substrings")
    var matchedSubstrings: Nothing? = null,
    @JsonProperty("reference")
    var reference: String? = null,
    @JsonProperty("structured_formatting")
    var structuredFormatting: StructuredFormatting? = null,
    @JsonProperty("terms")
    var terms: Nothing? = null,
    @JsonProperty("types")
    var types: ArrayList<String>? = null,
    @JsonProperty("place_id")
    var placeId: String? = null,
)

data class StructuredFormatting (
    @JsonProperty("main_text")
    var mainText: String? = null,
    @JsonProperty("main_text_matched_substrings")
    var mainTextMatchedSubstrings: Nothing? = null,
    @JsonProperty("secondary_text")
    var secondaryText: String? = null,
    @JsonProperty("secondary_text_matched_substrings")
    var secondaryTextMatchedSubstrings: Nothing? = null
    )
