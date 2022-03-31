package com.rw.random.domain.repository

import com.rw.random.common.constants.RedisKeyConstants
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Duration

interface RedisPubsubRepository {

    fun pubMessage(message: String): Mono<Void>

}

@Component
open class RedisPubsubRepositoryImpl(
    private val redisTemplate: ReactiveStringRedisTemplate
) : RedisPubsubRepository {


    override fun pubMessage(message: String): Mono<Void> {
        return redisTemplate.convertAndSend(RedisKeyConstants.REDIS_CHANNEL_KEY, message)
            .timeout(Duration.ofMillis(100))
            .retry(1)
            .onErrorResume { Mono.empty() }
            .then()
    }


}