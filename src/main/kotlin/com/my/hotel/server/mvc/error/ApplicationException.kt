package com.my.hotel.server.mvc.error

class ApplicationException( override val message: String) : RuntimeException(message)