package com.my.hotel.server.service.hotel.dto

import com.fasterxml.jackson.annotation.JsonProperty


data class AutoCompleteDTO(
    @JsonProperty("predictions")
    var predictions: ArrayList<PredictionDTO>? = null,
    @JsonProperty("status")
    var status: String? = null,
    @JsonProperty("error_message")
    var errorMessage: String? = null,
    @JsonProperty("info_messages")
    var infoMessages: ArrayList<String>? = null,
)
