package com.rw.websocket.domain.repository

import com.rw.random.common.constants.BeingStatus
import com.rw.random.common.entity.UserFish
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.query.Criteria.where
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface UserFishRepository {

    fun addOne(userId: Long, fishId: Long): Mono<UserFish>

    fun deleteOne(fishId: Long): Mono<Int>

    fun findFishOwner(fishId: Long): Mono<Long>

    fun findOne(fishId: Long): Mono<UserFish>

    fun findAll(userId: Long): Flux<UserFish>

    fun updateFishStatus(fishId: Long, status: BeingStatus): Mono<Int>
}

@Component
open class UserFishRepositoryImpl(
    private val entityTemplate: R2dbcEntityTemplate
) : UserFishRepository {
    override fun addOne(userId: Long, fishId: Long): Mono<UserFish> {
        return entityTemplate.insert(UserFish::class.java)
            .using(UserFish(null, userId, fishId, BeingStatus.ALIVE.ordinal))
    }

    override fun deleteOne(fishId: Long): Mono<Int> {
        return entityTemplate.delete(Query.query(where("fish_id").`is`(fishId)), UserFish::class.java)

    }

    override fun findFishOwner(fishId: Long): Mono<Long> {
        return findOne(fishId)
            .map {
                it.userId
            }
    }

    override fun findOne(fishId: Long): Mono<UserFish> {
        return entityTemplate.selectOne(
            Query.query(
                where("fish_id").`is`(fishId)
            ), UserFish::class.java
        )
    }

    override fun findAll(userId: Long): Flux<UserFish> {
        return entityTemplate.select(Query.query(where("user_id").`is`(userId)), UserFish::class.java)
    }

    override fun updateFishStatus(fishId: Long, status: BeingStatus): Mono<Int> {
        return entityTemplate.update(
            Query.query(where("fish_id").`is`(fishId)),
            Update.update("fish_status", status.ordinal),
            UserFish::class.java
        )
    }

}
