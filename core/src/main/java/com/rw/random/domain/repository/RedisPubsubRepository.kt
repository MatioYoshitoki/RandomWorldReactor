package com.rw.random.domain.repository

import com.rw.random.common.constants.RedisKeyConstants
import org.slf4j.LoggerFactory
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

    private val log = LoggerFactory.getLogger(javaClass)

    override fun pubMessage(message: String): Mono<Void> {
        log.debug("send message to channel: $message")

        return redisTemplate.convertAndSend(RedisKeyConstants.REDIS_CHANNEL_KEY, message)
            .timeout(Duration.ofMillis(100))
            .retry(1)
            .onErrorResume { Mono.empty() }
            .then()
    }


}
