package com.my.hotel.server

import com.bedatadriven.jackson.datatype.jts.JtsModule
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.client.RestTemplate

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableCaching
class ServerApplication {
	@Bean
	fun jtsModule(): JtsModule? {
		// This module will provide a Serializer for geometries
		return JtsModule()
	}
	@Bean
	fun passwordEncoder(): PasswordEncoder? {
		return BCryptPasswordEncoder()
	}
	@Bean
	fun restTemplate(): RestTemplate{
		return RestTemplate()
	}

}

fun main(args: Array<String>) {
	runApplication<ServerApplication>(*args)
}
