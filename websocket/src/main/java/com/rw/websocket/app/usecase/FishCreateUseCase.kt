package com.rw.websocket.app.usecase

import com.rw.random.common.dto.RWResult
import com.rw.websocket.app.service.UserService
import com.rw.websocket.domain.repository.FishRepository
import com.rw.websocket.domain.service.MoneyChangeService
import com.rw.websocket.infre.config.ApplicationProperties
import com.rw.websocket.infre.exception.EnterFishException
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.net.URI

interface FishCreateUseCase {
    fun runCase(userName: String): Mono<String>
}

@Component
open class FishCreateUseCaseImpl(
    private val applicationProperties: ApplicationProperties,
    private val moneyChangeService: MoneyChangeService,
    private val userService: UserService,
) : FishCreateUseCase {

    private val webClient = WebClient.create()

    override fun runCase(userName: String): Mono<String> {
        return userService.getUserWithPropertyByUserName(userName)
            .delayUntil { user ->
                userService.getAllFish(user.userId.toLong())
                    .count()
                    .flatMap {
                        if (user.fishMaxCount <= it) {
                            Mono.error(EnterFishException("拥有的鱼达到上限！"))
                        } else {
                            Mono.just(it)
                        }
                    }
            }
            .flatMap { user ->
                if (user.money <= applicationProperties.newFishPrice) {
                    Mono.error(EnterFishException("${applicationProperties.moneyName}不足！"))
                } else {
                    Mono.just(user)
                }
            }
            .flatMap { user ->
                expendMoney(user.userId.toLong(), userName)
            }
    }

    private fun expendMoney(userId: Long, userName: String): Mono<String> {
        return moneyChangeService.expendMoney(userId, userName, applicationProperties.newFishPrice)
            .flatMap { enoughMoney ->
                if (!enoughMoney) {
                    Mono.error(EnterFishException("${applicationProperties.moneyName}不足！"))
                } else {
                    requestCoreObjEnter(userId)
                        .flatMap { result ->
                            if (result.errno == 0) {
                                val fishId = result.data.toString().toLong()
                                userService.bindUserFish(userId, fishId)
                                    .map { fishId }
                            } else {
                                moneyChangeService.earnMoney(userId, userName, applicationProperties.newFishPrice)
                                    .flatMap {
                                        Mono.error(EnterFishException(result.message))
                                    }
                            }
                        }
                        .map { it.toString() }
                }
            }
    }

    private fun requestCoreObjEnter(masterId: Long): Mono<RWResult<*>> {
        return webClient.post()
            .uri(URI.create(applicationProperties.coreUrl + "/api/v1/object/enter?master_id=$masterId"))
            .retrieve()
            .bodyToMono(RWResult::class.java)
            .filter { it.errno == 0 }
            .filter { it.data != null }
    }

}
