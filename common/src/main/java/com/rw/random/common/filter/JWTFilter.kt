package com.rw.random.common.filter

import com.rw.random.common.constants.HttpConstants
import com.rw.random.common.constants.HttpConstants.ACCESS_TOKEN_PARAM
import com.rw.random.common.security.TokenProvider
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.util.StringUtils
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 * Filters incoming requests and installs a Spring Security principal if a header corresponding to a valid user is
 * found.
 *
 * @author liuting
 */
open class JWTFilter(tokenProvider: TokenProvider) : WebFilter {
    private val tokenProvider: TokenProvider

    init {
        this.tokenProvider = tokenProvider
    }

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val jwt = resolveToken(exchange.request)
        if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
            val authentication: Authentication = tokenProvider.getAuthentication(jwt)
            return chain.filter(exchange)
                .subscriberContext(ReactiveSecurityContextHolder.withAuthentication(authentication))
        }
        return chain.filter(exchange)
    }

    private fun resolveToken(request: ServerHttpRequest): String? {
        val bearerToken = request.headers.getFirst(HttpConstants.AUTHORIZATION_HEADER)
        if (!bearerToken.isNullOrBlank() && bearerToken.startsWith(HttpConstants.BEARER_PREFIX)) {
            return bearerToken.substring(7)
        }
        val queryParams = request.queryParams
        return if (queryParams.containsKey(ACCESS_TOKEN_PARAM)) {
            queryParams[ACCESS_TOKEN_PARAM]!![0] // 取第一个
        } else null
    }
}
