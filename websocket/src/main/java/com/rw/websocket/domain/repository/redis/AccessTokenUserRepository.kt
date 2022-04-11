package com.rw.websocket.domain.repository.redis

import com.rw.random.common.constants.RedisKeyConstants
import com.rw.websocket.domain.entity.UserWithProperty
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface AccessTokenUserRepository {

    fun findOneUserProperty(accessToken: String): Mono<UserWithProperty>

    fun addAll(accessToken: String, map: Map<String, String>): Mono<Boolean>

    fun removeOne(accessToken: String): Mono<Void>

}

@Component
open class AccessTokenUserRepositoryImpl(
    private val redisTemplate: ReactiveStringRedisTemplate
) : AccessTokenUserRepository {
    override fun findOneUserProperty(accessToken: String): Mono<UserWithProperty> {
        return redisTemplate.opsForHash<String, String>()
            .entries(getKey(accessToken))
            .collectList()
            .map { list ->
                list.associate { Pair(it.key, it.value) }
            }
            .filter { it.containsKey(UserWithProperty.USER_ID_FIELD) && it.containsKey(UserWithProperty.ACCESS_TOKEN_FIELD) }
            .map {
                UserWithProperty(
                    it[UserWithProperty.USER_ID_FIELD]!!.toLong(),
                    it[UserWithProperty.USER_NAME_FIELD] ?: "",
                    it[UserWithProperty.ACCESS_TOKEN_FIELD]!!,
                    it[UserWithProperty.EXP_FIELD]?.toLong() ?: 0L,
                    it[UserWithProperty.MONEY_FIELD]?.toLong() ?: 0L,
                    it[UserWithProperty.FISH_MAX_COUNT_FIELD]?.toLong() ?: 1L
                )
            }
    }

    override fun addAll(accessToken: String, map: Map<String, String>): Mono<Boolean> {
        return redisTemplate.opsForHash<String, String>()
            .putAll(getKey(accessToken), map)
    }

    override fun removeOne(accessToken: String): Mono<Void> {
        return redisTemplate.opsForHash<String, String>()
            .delete(getKey(accessToken))
            .then()
    }

    private fun getKey(accessToken: String): String {
        return RedisKeyConstants.ACCESS_TOKEN_USER + accessToken
    }

}