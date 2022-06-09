package com.rw.websocket.domain.repository

import cn.hutool.core.lang.Snowflake
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

    fun addOne(userName: String, password: String): Mono<User>

    fun findOneByUserName(userName: String): Mono<User>

    fun updateAccessToken(userId: Long, accessToken: String): Mono<Int>

    fun findUserWithPropertyFromDB(userId: Long): Mono<MutableMap<String, String>>

}

@Component
open class UserRepositoryImpl(
    private val entityTemplate: R2dbcEntityTemplate,
    private val snowflake: Snowflake,
) : UserRepository {

    override fun addOne(userName: String, password: String): Mono<User> {
        return entityTemplate.insert(User(snowflake.nextId(), userName, password, null))
    }

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

    override fun updateAccessToken(userId: Long, accessToken: String): Mono<Int> {
        return entityTemplate.update(
            Query.query(where("id").`is`(userId)),
            Update.update("access_token", accessToken),
            User::class.java
        )
    }

    override fun findUserWithPropertyFromDB(userId: Long): Mono<MutableMap<String, String>> {
        return entityTemplate.databaseClient
            .sql(
                """
                    select b.exp, b.fish_max_count, b.money, a.id as `user_id`, a.user_name from user a 
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

