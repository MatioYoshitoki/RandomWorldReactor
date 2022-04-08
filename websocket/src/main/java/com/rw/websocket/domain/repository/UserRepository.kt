package com.rw.websocket.domain.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.rw.random.common.constants.RedisKeyConstants
import com.rw.websocket.domain.entity.User
import com.rw.websocket.domain.entity.UserWithProperty
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.query.Criteria.where
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface UserRepository {

    fun findOneByUserName(userName: String): Mono<User>

    fun updateAccessToken(userId: Long, accessToken: String): Mono<UserWithProperty>

    fun findAccessTokenByUserId(userId: Long): Mono<String>

    fun findUserWithPropertyByToken(accessToken: String): Mono<UserWithProperty>

    fun updateUserWithProperty(accessToken: String, update: Map<String, String>): Mono<Boolean>

}

@Component
open class UserRepositoryImpl(
    private val entityTemplate: R2dbcEntityTemplate,
    private val redisTemplate: ReactiveStringRedisTemplate,
    private val objectMapper: ObjectMapper
) : UserRepository {


    override fun findOneByUserName(userName: String): Mono<User> {
        return entityTemplate.select(User::class.java)
            .matching(
                Query.query(
                    where("user_name").`is`(userName)
                )
                    .columns("id")
                    .columns("user_name")
                    .columns("password")
                    .columns("access_token")
            )
            .first()
    }

    override fun updateAccessToken(userId: Long, accessToken: String): Mono<UserWithProperty> {
        return entityTemplate.update(
            Query.query(where("id").`is`(userId)),
            Update.update("access_token", accessToken),
            User::class.java
        )
            .filter { it >= 1 }
            .delayUntil {
                redisTemplate.opsForValue()
                    .set(getUserAccessTokenKey(userId), accessToken)
            }
            .flatMap {
                findUserWithPropertyFromDB(userId)
                    .delayUntil {
                        redisTemplate.opsForHash<String, String>()
                            .putAll(getAccessTokenUserKey(accessToken), it)
                    }
                    .map {
                        UserWithProperty(
                            userId,
                            it[UserWithProperty.USER_NAME_FIELD]!!,
                            accessToken,
                            it[UserWithProperty.EXP_FIELD]?.toLong() ?: 0,
                            it[UserWithProperty.MONEY_FIELD]?.toLong() ?: 0,
                            it[UserWithProperty.FISH_MAX_COUNT_FIELD]?.toLong() ?: 0
                        )
                    }
            }

    }

    override fun findAccessTokenByUserId(userId: Long): Mono<String> {
        return redisTemplate.opsForValue()
            .get(getUserAccessTokenKey(userId))
    }

    override fun findUserWithPropertyByToken(accessToken: String): Mono<UserWithProperty> {
        return redisTemplate.opsForHash<String, String>()
            .entries(getAccessTokenUserKey(accessToken))
            .collectList()
            .map { list ->
                println(list)
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

    override fun updateUserWithProperty(accessToken: String, update: Map<String, String>): Mono<Boolean> {
        return redisTemplate.opsForHash<String, String>()
            .putAll(getAccessTokenUserKey(accessToken), update)
    }

    private fun getUserAccessTokenKey(userId: Long): String {
        return RedisKeyConstants.USER_ACCESS_TOKEN + userId
    }

    private fun getAccessTokenUserKey(token: String): String {
        return RedisKeyConstants.ACCESS_TOKEN_USER + token
    }

    private fun findUserWithPropertyFromDB(userId: Long): Mono<MutableMap<String, String>> {
        return entityTemplate.databaseClient
            .sql(
                """
                    select a.access_token, b.exp, b.fish_max_count, b.money, a.id as `user_id`, a.user_name  from user a 
                    left join user_property b on a.id = b.id
                    where a.id = $userId
                """.trimIndent()
            )
            .fetch()
            .first()
            .map { result ->
                val map = result.entries.associate {
                    Pair(it.key, it.value.toString())
                }.toMutableMap()
                map
            }
    }
}

