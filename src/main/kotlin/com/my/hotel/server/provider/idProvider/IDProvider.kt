package com.my.hotel.server.provider.idProvider

import org.springframework.stereotype.Component
import java.util.*

@Component
class IDProvider : IIDProvider {
    override fun getUUID(): String {
        return UUID.randomUUID().toString()
    }


}