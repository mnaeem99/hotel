package com.my.hotel.server.security

import com.my.hotel.server.data.repository.UserRepository
import com.my.hotel.server.security.jwt.*
import com.my.hotel.server.security.jwt.checkCredentialType.AuthenticateFactory
import com.my.hotel.server.security.oauth.AppleTokenVerifier
import com.my.hotel.server.security.oauth.FacebookTokenVerifier
import com.my.hotel.server.security.oauth.GoogleTokenVerifier
import com.my.hotel.server.security.providers.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfig @Autowired constructor(
    val userRepository: UserRepository,
    val emailAuthenticationProvider: EmailAuthenticationProvider,
    val phoneAuthenticationProvider: PhoneAuthenticationProvider,
    val googleAuthenticationProvider: GoogleAuthenticationProvider,
    val adminAuthenticationProvider: AdminAuthenticationProvider,
    val guestAuthenticationProvider: GuestAuthenticationProvider,
    val googleTokenVerify: GoogleTokenVerifier,
    val facebookTokenVerify: FacebookTokenVerifier,
    val appleTokenVerifier: AppleTokenVerifier,
    val securityUtils: SecurityUtils,
    val authenticateFactory: AuthenticateFactory,
    val jwtDecoder: JWTDecoder
) : WebSecurityConfigurerAdapter() {

    @Throws(Exception::class)
    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(emailAuthenticationProvider)
        auth.authenticationProvider(phoneAuthenticationProvider)
        auth.authenticationProvider(googleAuthenticationProvider)
        auth.authenticationProvider(adminAuthenticationProvider)
        auth.authenticationProvider(guestAuthenticationProvider)
    }
    override fun configure(http: HttpSecurity) {
        http.csrf().disable().cors().configurationSource(corsConfigurationSource()).and().headers().frameOptions().sameOrigin()
        http.sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        http.authorizeRequests()
            .antMatchers("/healthcheck").permitAll()
            .antMatchers("/api/image/**").permitAll()
            .antMatchers("/api/qr/**").permitAll()
            .antMatchers("/api/accessToken").permitAll()
            .antMatchers("/api/social/login").permitAll()
            .antMatchers(HttpMethod.POST, "/api/login").permitAll()
            .antMatchers(HttpMethod.POST, "/api/admin/login").permitAll()
            .antMatchers(HttpMethod.POST, "/api/guest/login").permitAll()
            .antMatchers(HttpMethod.POST, "/api/graphql").permitAll()
            .anyRequest()
            .authenticated()
            .and()
            .addFilterBefore(JWTAuthorizationFilter(authenticationManager(),googleTokenVerify, facebookTokenVerify,appleTokenVerifier,jwtDecoder), UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(JWTAuthenticationFilter(authenticationManager(), securityUtils,authenticateFactory), JWTAuthorizationFilter::class.java)
            .addFilterBefore(JWTAdminAuthenticationFilter(authenticationManager(), securityUtils,authenticateFactory), JWTAuthorizationFilter::class.java)
            .addFilterBefore(JWTGuestAuthenticationFilter(authenticationManager(), securityUtils,authenticateFactory), JWTAuthorizationFilter::class.java)
    }
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf(CorsConfiguration.ALL)
        configuration.allowedHeaders = listOf(CorsConfiguration.ALL)
        configuration.allowedMethods = listOf(CorsConfiguration.ALL)
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/api/graphql", configuration);
        source.registerCorsConfiguration("/api/admin/login", configuration);
        return source
    }
}
