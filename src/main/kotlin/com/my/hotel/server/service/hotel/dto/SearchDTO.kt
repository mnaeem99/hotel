package com.my.hotel.server.service.hotel.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class SearchDTO(
    @JsonProperty("html_attributions")
    var htmlAttributions: ArrayList<Any>? = null,
    @JsonProperty("results")
    var results: ArrayList<ResultDTO>? = null,
    @JsonProperty("status")
    var status: String? = null,
    @JsonProperty("error_message")
    var errorMessage: String? = null,
    @JsonProperty("info_messages")
    var infoMessages: ArrayList<String>? = null,
    @JsonProperty("next_page_token")
    var nextPageToken: String? = null
)
