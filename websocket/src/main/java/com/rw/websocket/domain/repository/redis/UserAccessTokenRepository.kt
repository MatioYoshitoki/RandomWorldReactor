package com.rw.websocket.domain.repository.redis

import com.rw.random.common.constants.RedisKeyConstants
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface UserAccessTokenRepository {

    fun findOne(userId: Long): Mono<String>

    fun addOne(userId: Long, accessToken: String): Mono<Boolean>

}

@Component
open class UserAccessTokenRepositoryImpl(
    private val redisTemplate: ReactiveStringRedisTemplate
) : UserAccessTokenRepository {
    override fun findOne(userId: Long): Mono<String> {
        return redisTemplate.opsForValue()
            .get(getKey(userId))
    }

    override fun addOne(userId: Long, accessToken: String): Mono<Boolean> {
        return redisTemplate.opsForValue()
            .set(getKey(userId), accessToken)
    }

    private fun getKey(userId: Long): String {
        return RedisKeyConstants.USER_ACCESS_TOKEN + userId
    }

}