package com.rw.websocket.app.usecase

import com.rw.random.common.dto.RWResult
import com.rw.websocket.app.service.UserService
import com.rw.websocket.domain.service.MoneyChangeService
import com.rw.websocket.infre.config.ApplicationProperties
import com.rw.websocket.infre.exception.EnterFishException
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.net.URI

interface FishCreateUseCase {
    fun runCase(accessToken: String): Mono<Long>
}

@Component
open class FishCreateUseCaseImpl(
    private val applicationProperties: ApplicationProperties,
    private val moneyChangeService: MoneyChangeService,
    private val userService: UserService,
) : FishCreateUseCase {

    private val webClient = WebClient.create()

    override fun runCase(accessToken: String): Mono<Long> {
        return userService.getUserWithPropertyByAccessToken(accessToken)
            .delayUntil { user ->
                userService.getAllFish(user.userId)
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
                expendMoney(user.userId)
            }
    }

    private fun expendMoney(userId: Long): Mono<Long> {
        return moneyChangeService.expendMoney(userId, applicationProperties.newFishPrice)
            .flatMap { enoughMoney ->
                if (!enoughMoney) {
                    Mono.error(EnterFishException("${applicationProperties.moneyName}不足！"))
                } else {
                    requestCoreObjEnter()
                        .flatMap { result ->
                            if (result.errno == 0) {
                                val fishId = result.data.toString().toLong()
                                userService.bindUserFish(userId, fishId)
                                    .map { fishId }
                            } else {
                                moneyChangeService.earnMoney(userId, applicationProperties.newFishPrice)
                                    .flatMap {
                                        Mono.error(EnterFishException(result.message))
                                    }
                            }
                        }
                }
            }
    }

    private fun requestCoreObjEnter(): Mono<RWResult<*>> {
        return webClient.post()
            .uri(URI.create(applicationProperties.coreUrl + "/api/v1/object/enter"))
            .retrieve()
            .bodyToMono(RWResult::class.java)
            .filter { it.errno == 0 }
            .filter { it.data != null }
    }

}