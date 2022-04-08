package com.rw.websocket.app.usecase

import com.rw.random.common.constants.BeingStatus
import com.rw.websocket.app.service.FishService
import com.rw.websocket.app.service.UserService
import com.rw.websocket.domain.repository.UserFishRepository
import com.rw.websocket.domain.repository.UserRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface EatFishUseCase {

    fun runCase(accessToken: String, fishId: Long): Mono<Boolean>

}

@Component
open class EatFishUseCaseImpl(
    private val userFishRepository: UserFishRepository,
    private val userRepository: UserRepository,
    private val userService: UserService,
) : EatFishUseCase {
    override fun runCase(accessToken: String, fishId: Long): Mono<Boolean> {
        return userRepository.findUserWithPropertyByToken(accessToken)
            .flatMap { user ->
                userFishRepository.findOne(fishId)
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