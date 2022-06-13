package com.rw.websocket.infre.config

import com.rw.random.common.config.RWSecurityProperties
import com.rw.random.common.security.RWReactiveAccessManager
import com.rw.random.common.security.TokenProvider
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@EnableConfigurationProperties(RWSecurityProperties::class)
open class RWSecurityAutoConfiguration(
    private val securityProperties: RWSecurityProperties
) {
    @Bean
    open fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.`$2Y`, 13)
    }

    @Bean
    open fun accessManager(): RWReactiveAccessManager {
        return RWReactiveAccessManager(securityProperties)
    }

    @Bean
    open fun tokenProvider(): TokenProvider {
        return TokenProvider(securityProperties)
    }

}
