package com.rw.random.infra.config

import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.context.annotation.Configuration

@Configuration
open class RedisConfiguration(
    private val redisProperties: RedisProperties
) {

//    @Bean
//    open fun masterTemplate(connectionFactory: ReactiveRedisConnectionFactory): ReactiveStringRedisTemplate {
//        return ReactiveStringRedisTemplate(connectionFactory)
//    }

}
