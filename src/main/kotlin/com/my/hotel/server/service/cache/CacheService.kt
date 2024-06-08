package com.my.hotel.server.service.cache

import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service


@Service
@Slf4j
class CacheService @Autowired constructor(
    private val cacheManager: CacheManager,
//    private val redisTemplate: RedisTemplate<String, String>
): ICacheService {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)

    override fun evictAllCaches() {
        logger.info("Evict All Caches")
        cacheManager.cacheNames.stream().forEach { cacheName: String? -> cacheManager.getCache(cacheName!!)?.clear() }
    }
    override fun evictCache(name: String) {
//        val ks = redisTemplate.keys("*")
//        val keys = redisTemplate.connectionFactory?.connection?.keys("*".toByteArray())
        cacheManager.getCache(name)?.clear()
    }


}