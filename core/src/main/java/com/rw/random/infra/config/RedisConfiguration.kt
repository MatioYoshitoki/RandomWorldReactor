package com.rw.random.infra.config

import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveStringRedisTemplate

@Configuration
open class RedisConfiguration(
    private val redisProperties: RedisProperties
) {

//    @Bean
//    open fun masterTemplate(connectionFactory: ReactiveRedisConnectionFactory): ReactiveStringRedisTemplate {
//        return ReactiveStringRedisTemplate(connectionFactory)
//    }

}