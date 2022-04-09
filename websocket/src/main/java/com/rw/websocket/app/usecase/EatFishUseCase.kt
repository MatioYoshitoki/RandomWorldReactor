package com.rw.websocket.app.usecase

import com.rw.random.common.constants.BeingStatus
import com.rw.websocket.app.service.UserService
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface EatFishUseCase {

    fun runCase(accessToken: String, fishId: Long): Mono<Boolean>

}

@Component
open class EatFishUseCaseImpl(
    private val userService: UserService,
) : EatFishUseCase {
    override fun runCase(accessToken: String, fishId: Long): Mono<Boolean> {
        return userService.getUserWithPropertyByAccessToken(accessToken)
            .flatMap { user ->
                userService.getUserFish(fishId)
                    .filter {
                        user.userId == it.userId
                    }
                    .filter { it.fishStatus == BeingStatus.SLEEP.ordinal }
            }
            .flatMap {
                userService.eatFish(fishId, it.userId, accessToken)
            }
    }
}