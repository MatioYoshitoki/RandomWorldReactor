package com.rw.websocket.app.usecase

import com.rw.random.common.constants.BeingStatus
import com.rw.websocket.app.service.FishService
import com.rw.websocket.app.service.UserService
import com.rw.websocket.infre.exception.FishStatusException
import com.rw.websocket.infre.exception.NotFishOwnerException
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface SellFishUseCase {

    fun runCase(userName: String, fishId: Long, price: Long): Mono<Void>

}

@Component
open class SellFishUseCaseImpl(
    private val fishService: FishService,
    private val userService: UserService,
) : SellFishUseCase {
    override fun runCase(userName: String, fishId: Long, price: Long): Mono<Void> {
        return fishService.checkFishOwner(fishId, userName)
            .flatMap {
                if (it) {
                    fishService.getFishDetail(fishId)
                } else {
                    Mono.error(NotFishOwnerException())
                }
            }
            .flatMap { fish ->
                if (fish.status == BeingStatus.SLEEP) {
                    userService.getUserByUserName(userName)
                        .flatMap {
                            fishService.sellFish(fish, userName, it.id, price)
                        }
                } else {
                    Mono.error(FishStatusException())
                }
            }
    }
}
