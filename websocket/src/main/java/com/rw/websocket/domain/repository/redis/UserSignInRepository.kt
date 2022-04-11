package com.rw.websocket.domain.repository.redis

import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty
import java.time.Duration

interface UserSignInRepository {

    fun exist(userId: Long, date: String): Mono<Boolean>

    fun addOne(userId: Long, date: String): Mono<Void>

}

@Component
open class UserSignInRepositoryImpl(
    private val redisTemplate: ReactiveStringRedisTemplate
) : UserSignInRepository {
    override fun exist(userId: Long, date: String): Mono<Boolean> {
        return redisTemplate.opsForSet()
            .isMember(getKey(date), userId.toString())
    }

    override fun addOne(userId: Long, date: String): Mono<Void> {
        return redisTemplate.opsForSet()
            .size(getKey(date))
            .filter { it > 0 }
            .delayUntil {
                redisTemplate.opsForSet()
                    .add(getKey(date), userId.toString())
            }
            .switchIfEmpty {
                redisTemplate.opsForSet()
                    .add(getKey(date), userId.toString())
                    .delayUntil { redisTemplate.expire(getKey(date), Duration.ofHours(25)) }
            }
            .then()
    }

    private fun getKey(date: String): String {
        return "user_sign_in:$date"
    }
}