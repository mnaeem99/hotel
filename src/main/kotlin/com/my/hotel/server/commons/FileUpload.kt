package com.my.hotel.server.commons

data class FileUpload constructor(var contentType: String, var content: ByteArray) {
}