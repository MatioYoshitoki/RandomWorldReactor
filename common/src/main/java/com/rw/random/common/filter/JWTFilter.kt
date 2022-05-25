package com.rw.random.common.filter

import com.rw.random.common.constants.HttpConstants.ACCESS_TOKEN_PARAM
import com.rw.random.common.constants.HttpConstants.AUTHORIZATION_HEADER
import com.rw.random.common.security.TokenProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.util.StringUtils
import org.springframework.web.filter.GenericFilterBean
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

/**
 * Filters incoming requests and installs a Spring Security principal if a header corresponding to a valid user is
 * found.
 */
class JWTFilter(tokenProvider: TokenProvider) : GenericFilterBean() {
    private val tokenProvider: TokenProvider

    init {
        this.tokenProvider = tokenProvider
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        val httpServletRequest: HttpServletRequest = servletRequest as HttpServletRequest
        val jwt = resolveToken(httpServletRequest)
        if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
            val authentication: Authentication = tokenProvider.getAuthentication(jwt)
            SecurityContextHolder.getContext().authentication = authentication
        }
        filterChain.doFilter(servletRequest, servletResponse)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken: String = request.getHeader(AUTHORIZATION_HEADER)
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7)
        }
        // 如果 header 中没有 Authorization，则从 uri 参数中获取，如果还没有，则从 body json 中获取
        return request.getParameter(ACCESS_TOKEN_PARAM)
    }
}
