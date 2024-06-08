package com.my.hotel.server.service.cache

interface ICacheService {
    fun evictAllCaches()
    fun evictCache(name: String)
}