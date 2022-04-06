package com.rw.websocket.domain.repository

import com.rw.random.common.constants.BeingStatus
import com.rw.random.common.entity.UserFish
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.query.Criteria.where
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface UserFishRepository {

    fun bindFish(userId: Long, fishId: Long): Mono<UserFish>

    fun poolFishCount(userId: Long): Mono<Long>

}

@Component
open class UserFishRepositoryImpl(
    private val entityTemplate: R2dbcEntityTemplate
) : UserFishRepository {
    override fun bindFish(userId: Long, fishId: Long): Mono<UserFish> {
        return entityTemplate.insert(UserFish::class.java)
            .using(UserFish(userId, fishId, BeingStatus.ALIVE.ordinal))
    }

    override fun poolFishCount(userId: Long): Mono<Long> {
        return entityTemplate.select(
            Query.query(where("user_id").`is`(userId)), UserFish::class.java
        ).count()
    }
}