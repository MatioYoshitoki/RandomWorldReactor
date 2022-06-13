package com.rw.random.common.security

import com.rw.random.common.config.RWSecurityProperties
import org.springframework.security.authentication.AuthenticationTrustResolver
import org.springframework.security.authentication.AuthenticationTrustResolverImpl
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.ReactiveAuthorizationManager
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authorization.AuthorizationContext
import org.springframework.util.AntPathMatcher
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

open class RWReactiveAccessManager(
    private val securityProperties: RWSecurityProperties
) : ReactiveAuthorizationManager<AuthorizationContext> {
    private val authTrustResolver: AuthenticationTrustResolver = AuthenticationTrustResolverImpl()
    private val defaultPermitUris: List<String> = listOf("/", "/error", "/favicon.ico")
    private val pathMatcher = AntPathMatcher()

    override fun check(
        authentication: Mono<Authentication>,
        authorizationContext: AuthorizationContext
    ): Mono<AuthorizationDecision> {
        return authentication
            .map { a: Authentication ->
                if (isNotAnonymous(a)) {
                    return@map AuthorizationDecision(a.isAuthenticated)
                } else {
                    val exchange = authorizationContext.exchange
                    return@map getDecision(exchange)
                }
            }
            .switchIfEmpty(Mono.just(getDecision(authorizationContext.exchange)))
    }

    private fun getDecision(exchange: ServerWebExchange): AuthorizationDecision {
        val requestPath = exchange.request.uri.path
        return if (checkPermits(requestPath)) {
            AuthorizationDecision(true)
        } else {
            AuthorizationDecision(false)
        }
    }

    /**
     * 用户已经登录
     *
     * @param authentication
     * @return
     */
    private fun isNotAnonymous(authentication: Authentication): Boolean {
        return !authTrustResolver.isAnonymous(authentication)
    }//        runtimePermitUris = new LinkedMultiValueMap<>();
//        permitAll.addAll(tempPermitUris.values());
//        runtimePermitUris.values().forEach(permitAll::addAll);
    /**
     * 将默认放行列表、配置列表、临时列表合并成一个
     *
     * @return
     */
    private val permitUris: List<String>
        get() {
            val permitAll: MutableList<String> = mutableListOf()
            //        runtimePermitUris = new LinkedMultiValueMap<>();
//        permitAll.addAll(tempPermitUris.values());
//        runtimePermitUris.values().forEach(permitAll::addAll);
            permitAll.addAll(securityProperties.authentication.permitAll)
            permitAll.addAll(defaultPermitUris)
            return permitAll
        }

    /**
     * 检查是否放行
     *
     * @param path
     * @return
     */
    private fun checkPermits(path: String): Boolean {
        val permit = permitUris.stream()
            .anyMatch { pathMatcher.match(it, path) }
        // TODO: 动态权限
        return if (permit) {
            checkDynamicPermission()
        } else {
            false
        }
    }

    /**
     * TODO: 校验动态权限
     *
     * @return
     */
    private fun checkDynamicPermission(): Boolean {
        // TODO
        return true
    }
}
