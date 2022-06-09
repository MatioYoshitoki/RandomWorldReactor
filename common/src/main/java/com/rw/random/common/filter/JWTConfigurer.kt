//package com.rw.random.common.filter
//
//import com.rw.random.common.security.TokenProvider
//import org.springframework.security.config.annotation.SecurityConfigurerAdapter
//import org.springframework.security.config.annotation.web.builders.HttpSecurity
//import org.springframework.security.web.DefaultSecurityFilterChain
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
//
//class JWTConfigurer(tokenProvider: TokenProvider) :
//    SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>() {
//    private val tokenProvider: TokenProvider
//
//    init {
//        this.tokenProvider = tokenProvider
//    }
//
//    override fun configure(http: HttpSecurity) {
//        val customFilter = JWTFilter(tokenProvider)
//        http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter::class.java)
//    }
//}
