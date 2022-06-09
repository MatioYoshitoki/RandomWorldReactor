package com.rw.random.common.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@EnableConfigurationProperties(RWSecurityProperties::class)
open class RWSecurityAutoConfiguration {
    @Bean
    open fun passwordEncoder(): PasswordEncoder {
//        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
        return BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.`$2Y`, 13)
    }


}
