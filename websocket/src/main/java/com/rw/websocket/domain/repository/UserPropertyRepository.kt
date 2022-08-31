package com.rw.websocket.domain.repository

import com.rw.websocket.domain.entity.UserProperty
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.query.Criteria.where
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface UserPropertyRepository {

    fun addOne(userId: Long): Mono<UserProperty>

    fun findOne(userId: Long): Mono<UserProperty>

    fun updateMoney(userId: Long, money: Long): Mono<Boolean>

    fun updateExp(userId: Long, exp: Long): Mono<Boolean>

}

@Component
open class UserPropertyRepositoryImpl(
    private val entityTemplate: R2dbcEntityTemplate
) : UserPropertyRepository {
    override fun addOne(userId: Long): Mono<UserProperty> {
        return entityTemplate.insert(UserProperty(id = userId, fishMaxCount = 1, money = 5000))
    }

    override fun findOne(userId: Long): Mono<UserProperty> {
        return entityTemplate.selectOne(Query.query(where("id").`is`(userId)), UserProperty::class.java)
    }

    override fun updateMoney(userId: Long, money: Long): Mono<Boolean> {
        return entityTemplate.update(
            Query.query(where("id").`is`(userId)),
            Update.update("money", money),
            UserProperty::class.java
        )
            .map { it == 1 }
    }

    override fun updateExp(userId: Long, exp: Long): Mono<Boolean> {
        return entityTemplate.update(
            Query.query(where("id").`is`(userId)),
            Update.update("exp", exp),
            UserProperty::class.java
        )
            .map { it == 1 }
    }

}
