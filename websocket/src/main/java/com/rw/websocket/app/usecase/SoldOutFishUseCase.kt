package com.rw.websocket.app.usecase

import com.rw.random.common.constants.BeingStatus
import com.rw.websocket.app.service.FishService
import com.rw.websocket.app.service.UserService
import com.rw.websocket.infre.exception.FishStatusException
import com.rw.websocket.infre.exception.NotFishOwnerException
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface SoldOutFishUseCase {

    fun runCase(userName: String, orderId: Long): Mono<Void>

}

@Component
open class SoldOutFishUseCaseImpl(
    private val fishService: FishService,
    private val userService: UserService,
) : SoldOutFishUseCase {
    override fun runCase(userName: String, orderId: Long): Mono<Void> {
        return fishService.getSellingFishByOrderId(orderId)
            .filter { it.status == 1 }
            .filterWhen { fishService.checkFishOwner(it.fishId, userName) }
            .flatMap { fishSellLog ->
                userService.getUserByUserName(userName)
                    .flatMap {
                        fishService.soldOutFish(userName, orderId, fishSellLog.fishId)
                    }
            }

    }

}