package com.rw.websocket.domain.repository.redis

import com.rw.random.common.constants.RedisKeyConstants
import com.rw.websocket.domain.entity.UserWithProperty
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface UserInfoRepository {

    fun findOneUserProperty(userId: Long): Mono<UserWithProperty>

    fun addAll(userId: Long, map: Map<String, String>): Mono<Boolean>

    fun removeOne(userId: Long): Mono<Void>

}

@Component
open class UserInfoRepositoryImpl(
    private val redisTemplate: ReactiveStringRedisTemplate
) : UserInfoRepository {
    override fun findOneUserProperty(userId: Long): Mono<UserWithProperty> {
        return redisTemplate.opsForHash<String, String>()
            .entries(getKey(userId))
            .collectList()
            .map { list ->
                list.associate { Pair(it.key, it.value) }
            }
            .filter { it.containsKey(UserWithProperty.USER_ID_FIELD) && it.containsKey(UserWithProperty.ACCESS_TOKEN_FIELD) }
            .map {
                UserWithProperty(
                    it[UserWithProperty.USER_ID_FIELD]!!.toLong(),
                    it[UserWithProperty.USER_NAME_FIELD] ?: "",
                    it[UserWithProperty.EXP_FIELD]?.toLong() ?: 0L,
                    it[UserWithProperty.MONEY_FIELD]?.toLong() ?: 0L,
                    it[UserWithProperty.FISH_MAX_COUNT_FIELD]?.toLong() ?: 1L
                )
            }
    }

    override fun addAll(userId: Long, map: Map<String, String>): Mono<Boolean> {
        return redisTemplate.opsForHash<String, String>()
            .putAll(getKey(userId), map)
    }

    override fun removeOne(userId: Long): Mono<Void> {
        return redisTemplate.opsForHash<String, String>()
            .delete(getKey(userId))
            .then()
    }

    private fun getKey(userId: Long): String {
        return RedisKeyConstants.USER_INFO + userId
    }

}
