package com.rw.random.domain.repository

import com.rw.random.common.constants.BeingStatus
import com.rw.random.common.entity.UserFish
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.query.Criteria.where
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface UserFishRepository {

    fun addOne(userFish: UserFish): Mono<Void>

    fun deleteOne(fishId: Long): Mono<Boolean>

    fun updateStatus(fishId: Long, status: BeingStatus): Mono<Void>

}

@Component
open class UserFishRepositoryImpl(
    private val entityTemplate: R2dbcEntityTemplate
) : UserFishRepository {
    override fun addOne(userFish: UserFish): Mono<Void> {
        return entityTemplate.insert(userFish).then()
    }

    override fun deleteOne(fishId: Long): Mono<Boolean> {
        return entityTemplate.delete(Query.query(where("fish_id").`is`(fishId)), UserFish::class.java).map { true }
            .defaultIfEmpty(false)
    }

    override fun updateStatus(fishId: Long, status: BeingStatus): Mono<Void> {
        return entityTemplate.update(
            Query.query(where("fish_id").`is`(fishId)), Update.update("fish_status", status.ordinal), UserFish::class.java
        ).then()
    }
}