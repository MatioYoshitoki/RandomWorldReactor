package com.rw.websocket.app.usecase

import com.rw.random.common.constants.BeingStatus
import com.rw.websocket.app.service.FishService
import com.rw.websocket.app.service.UserService
import com.rw.websocket.domain.entity.FishSellLog
import com.rw.websocket.domain.entity.UserWithProperty
import com.rw.websocket.domain.service.MoneyChangeService
import com.rw.websocket.domain.service.NormalLockService
import com.rw.websocket.infre.exception.FishLimitedException
import com.rw.websocket.infre.exception.NotEnoughMoneyException
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty
import reactor.core.scheduler.Schedulers

interface BuyFishUseCase {

    fun runCase(userName: String, orderId: Long): Mono<FishSellLog>

}

@Component
open class BuyFishUseCaseImpl(
    private val userService: UserService,
    private val fishService: FishService,
    private val moneyChangeService: MoneyChangeService,
    private val normalLockService: NormalLockService,
) : BuyFishUseCase {

    override fun runCase(userName: String, orderId: Long): Mono<FishSellLog> {
        return normalLockService.tryLock(orderId)
            .filter { it }
            .flatMap {
                fishService.getSellingFishByOrderId(orderId)
                    .filter { it.status == 0 }
                    .flatMap { fishSellLog ->
                        userService.getUserWithPropertyByUserName(userName)
                            .flatMap { property ->
                                if (property.money >= fishSellLog.price) {
                                    userService.getAllFish(property.userId.toLong())
                                        .count()
                                        .flatMap { fishCount ->
                                            if (property.fishMaxCount > fishCount) {
                                                buy(property, fishSellLog)
                                            } else {
                                                Mono.error(FishLimitedException())
                                            }
                                        }
                                } else {
                                    Mono.error(NotEnoughMoneyException())
                                }
                            }
                    }
            }
            .switchIfEmpty {
                Mono.error(Exception("trading failed!"))
            }
            .publishOn(Schedulers.boundedElastic())
            .doFinally {
                normalLockService.release(orderId).block()
            }

    }

    private fun buy(property: UserWithProperty, fishSellLog: FishSellLog): Mono<FishSellLog> {
        return moneyChangeService.expendMoney(
            property.userId.toLong(),
            property.userName,
            fishSellLog.price.toLong()
        )
            .flatMap { success ->
                if (success) {
                    changeFishMaster(fishSellLog, property)
                        .filter { it }
                        .flatMap {
                            fishService.finishFishOrder(
                                property.userId.toLong(),
                                property.userName,
                                fishSellLog
                            )
                        }
                } else {
                    Mono.error(NotEnoughMoneyException())
                }
            }
            .filter { it }
            .flatMap {
                fishService.changeFishStatus(fishSellLog.fishId.toLong(), BeingStatus.SLEEP)
            }
            .map { fishSellLog }
    }

    private fun changeFishMaster(fishSellLog: FishSellLog, property: UserWithProperty): Mono<Boolean> {
        return userService.unbindUserFish(
            fishSellLog.sellerId.toLong(),
            fishSellLog.fishId.toLong()
        )
            .filter { it }
            .flatMap {
                userService.bindUserFish(
                    property.userId.toLong(),
                    fishSellLog.fishId.toLong()
                )
            }
            .flatMap {
                fishService.finishFishOrder(
                    property.userId.toLong(),
                    property.userName,
                    fishSellLog
                )
            }
            .defaultIfEmpty(false)
    }
}
