package com.my.hotel.server.service.hotel.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class DetailsDTO (
    @JsonProperty("html_attributions")
    var htmlAttributions: ArrayList<Any>? = null,
    @JsonProperty("result")
    var result: ResultDTO? = null,
    @JsonProperty("status")
    var status: String? = null,
    @JsonProperty("info_messages")
    var infoMessages: ArrayList<String>? = null,
)
