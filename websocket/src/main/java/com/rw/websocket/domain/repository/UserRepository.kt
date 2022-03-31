package com.rw.websocket.domain.repository

import com.rw.random.common.constants.RedisKeyConstants
import com.rw.websocket.domain.entity.User
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.query.Criteria.where
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface UserRepository {

    fun findOneByUserName(userName: String): Mono<User>

    fun updateAccessToken(userId: Long, accessToken: String): Mono<String>

    fun findIdByToken(accessToken: String): Mono<Long>

}

@Component
open class UserRepositoryImpl(
    private val entityTemplate: R2dbcEntityTemplate,
    private val redisTemplate: ReactiveStringRedisTemplate,
) : UserRepository {


    override fun findOneByUserName(userName: String): Mono<User> {
        return entityTemplate.selectOne(
            Query.query(
                where("user_name").`is`(userName)
            )
                .columns("id")
                .columns("user_name")
                .columns("password")
                .columns("access_token"),
            User::class.java
        )
    }

    override fun updateAccessToken(userId: Long, accessToken: String): Mono<String> {
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
            .map { accessToken }
    }

    override fun findIdByToken(accessToken: String): Mono<Long> {
        return redisTemplate.opsForValue()
            .get(getAccessTokenUserKey(accessToken))
            .map { it.toLong() }
    }

    private fun getUserAccessTokenKey(userId: Long): String {
        return RedisKeyConstants.USER_ACCESS_TOKEN + userId
    }

    private fun getAccessTokenUserKey(token: String): String {
        return RedisKeyConstants.ACCESS_TOKEN_USER + token
    }
}

