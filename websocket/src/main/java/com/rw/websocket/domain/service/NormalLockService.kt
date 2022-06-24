package com.rw.websocket.domain.service

import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.Objects.hash

interface NormalLockService {

    fun tryLock(lock: Any): Mono<Boolean>

    fun release(lock: Any): Mono<Void>

}

@Service
open class NormalLockServiceImpl(
    private val redisTemplate: ReactiveStringRedisTemplate
) : NormalLockService {
    override fun tryLock(lock: Any): Mono<Boolean> {
        return redisTemplate.opsForValue()
            .setIfAbsent(getKey(lock), "1")
            .delayUntil {
                if (it) {
                    redisTemplate.expire(getKey(lock), Duration.ofSeconds(3))
                } else {
                    Mono.empty<Boolean>()
                }
            }
    }

    override fun release(lock: Any): Mono<Void> {
        return redisTemplate.opsForValue()
            .delete(getKey(lock))
            .then()
    }

    private fun getKey(lock: Any): String {
        return "lock:normal:${hash(lock)}"
    }
}
