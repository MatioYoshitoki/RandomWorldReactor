package com.rw.websocket.domain.repository

import com.rw.random.common.constants.BeingStatus
import com.rw.random.common.entity.UserFish
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface UserFishRepository {

    fun bindFish(userId: Long, fishId: Long): Mono<UserFish>

}

@Component
open class UserFishRepositoryImpl(
    private val entityTemplate: R2dbcEntityTemplate
) : UserFishRepository {
    override fun bindFish(userId: Long, fishId: Long): Mono<UserFish> {
        return entityTemplate.insert(UserFish::class.java)
            .using(UserFish(userId, fishId, BeingStatus.ALIVE.ordinal))
    }
}