package com.my.hotel.server.mvc

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ImageController {

    @PostMapping("/api/image/profile")
    fun uploadProfilePhoto() : String {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        val currentPrincipalName: String = authentication.name
        return "Hello : $currentPrincipalName "
    }

    @PostMapping("/api/image/hotel")
    fun uploadHotelPhoto() : String {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        val currentPrincipalName: String = authentication.name
        return "Hello : $currentPrincipalName "
    }

}