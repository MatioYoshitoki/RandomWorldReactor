package com.rw.websocket.domain.repository.redis

import com.rw.random.common.constants.RedisKeyConstants
import com.rw.websocket.domain.entity.UserWithProperty
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface UserInfoRepository {

    fun findOneUserProperty(userName: String): Mono<UserWithProperty>

    fun addAll(userName: String, map: Map<String, String>): Mono<Boolean>

    fun removeOne(userName: String): Mono<Void>

}

@Component
open class UserInfoRepositoryImpl(
    private val redisTemplate: ReactiveStringRedisTemplate
) : UserInfoRepository {
    override fun findOneUserProperty(userName: String): Mono<UserWithProperty> {
        return redisTemplate.opsForHash<String, String>()
            .entries(getKey(userName))
            .collectList()
            .map { list ->
                list.associate { Pair(it.key, it.value) }
            }
            .filter { it.containsKey(UserWithProperty.USER_ID_FIELD) }
            .map {
                UserWithProperty(
                    it[UserWithProperty.USER_ID_FIELD]!!.toString(),
                    it[UserWithProperty.USER_NAME_FIELD] ?: "",
                    it[UserWithProperty.EXP_FIELD]?.toInt() ?: 0,
                    it[UserWithProperty.MONEY_FIELD]?.toInt() ?: 0,
                    it[UserWithProperty.FISH_MAX_COUNT_FIELD]?.toLong() ?: 1L
                )
            }
    }

    override fun addAll(userName: String, map: Map<String, String>): Mono<Boolean> {
        return redisTemplate.opsForHash<String, String>()
            .putAll(getKey(userName), map)
    }

    override fun removeOne(userName: String): Mono<Void> {
        return redisTemplate.opsForHash<String, String>()
            .delete(getKey(userName))
            .then()
    }

    private fun getKey(userName: String): String {
        return RedisKeyConstants.USER_INFO + userName
    }

}
