package com.rw.websocket.infre.config

//import com.rw.random.common.filter.JWTConfigurer
import com.rw.random.common.constants.AuthoritiesConstants
import com.rw.random.common.filter.JWTFilter
import com.rw.random.common.security.RWReactiveAccessManager
import com.rw.random.common.security.TokenProvider
import com.rw.websocket.domain.repository.UserRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.zalando.problem.spring.webflux.advice.security.SecurityProblemSupport

//import org.zalando.problem.spring.webflux.advice.security.SecurityProblemSupport;


@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Import(SecurityProblemSupport::class)
open class WebsocketSecurityConfiguration(
    private val tokenProvider: TokenProvider,
    private val problemSupport: SecurityProblemSupport,
    private val accessManager: RWReactiveAccessManager
) {


    @Bean
    @ConditionalOnMissingBean
    open fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        // @formatter:off
        http
            .cors()
            .and()
            .securityMatcher(
                NegatedServerWebExchangeMatcher(
                    OrServerWebExchangeMatcher(
                        ServerWebExchangeMatchers.pathMatchers(
                            "/app/**",
                            "/i18n/**",
                            "/content/**",
                            "/swagger-ui/**",
                            "/test/**"
                        ),
                        ServerWebExchangeMatchers.pathMatchers(HttpMethod.OPTIONS, "/**")
                    )
                )
            )
            .csrf()
            .disable()
            .addFilterAt(JWTFilter(tokenProvider), SecurityWebFiltersOrder.FORM_LOGIN)
            .exceptionHandling()
            .accessDeniedHandler(problemSupport)
            .authenticationEntryPoint(problemSupport)
            .and()
            .headers()
            .contentSecurityPolicy("default-src 'self'; frame-src 'self' data:; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage.googleapis.com; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:")
            .and()
            .referrerPolicy(ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
            .and()
            .featurePolicy("geolocation 'none'; midi 'none'; sync-xhr 'none'; microphone 'none'; camera 'none'; magnetometer 'none'; gyroscope 'none'; speaker 'none'; fullscreen 'self'; payment 'none'")
            .and()
            .frameOptions().disable()
            .and()
            .authorizeExchange()
            .pathMatchers("/").permitAll()
            .pathMatchers("/api/v1/user/login").permitAll()
            .pathMatchers("/api/v1/user/register").permitAll()
            .pathMatchers("/management/prometheus").permitAll()
            .pathMatchers("/management/**")
            .hasAuthority(AuthoritiesConstants.ADMIN) //                .pathMatchers("/**").authenticated();
            .pathMatchers("/**").access(accessManager)
//            .pathMatchers("/**").hasAuthority(AuthoritiesConstants.USER)
//                .anyExchange().access(accessManager);
        // @formatter:on
        return http.build()
    }


    @Bean
    open fun userDetailsService(userRepository: UserRepository): ReactiveUserDetailsService? {
        return ReactiveUserDetailsService { username ->
            userRepository.findOneByUserName(username)
                .map { u ->
                    User
                        .withUsername(u.userName).password(u.password)
                        .authorities(AuthoritiesConstants.USER)
//                        .authorities(u..toArray(arrayOfNulls<String>(0)))
//                        .accountExpired(!u.isActive())
//                        .credentialsExpired(!u.isActive())
//                        .disabled(!u.isActive())
//                        .accountLocked(!u.isActive())
                        .build()
                }
        }
    }

    @Bean
    open fun reactiveAuthenticationManager(
        userDetailsService: ReactiveUserDetailsService,
        passwordEncoder: PasswordEncoder?
    ): ReactiveAuthenticationManager {
        val authenticationManager = UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService)
        authenticationManager.setPasswordEncoder(passwordEncoder)
        return authenticationManager
    }

//    private fun securityConfigurerAdapter(): JWTConfigurer {
//        return JWTConfigurer(tokenProvider)
//    }
}
