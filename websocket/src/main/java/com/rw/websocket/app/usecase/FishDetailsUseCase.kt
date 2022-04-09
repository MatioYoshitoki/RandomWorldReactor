package com.rw.websocket.app.usecase

import com.rw.websocket.app.service.FishService
import com.rw.websocket.app.service.UserService
import com.rw.websocket.domain.dto.request.FishDetails
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface FishDetailsUseCase {

    fun runCase(accessToken: String, fishId: Long?): Mono<List<FishDetails>>

}

@Component
open class FishDetailsUseCaseImpl(
    private val fishService: FishService,
    private val userService: UserService,
) : FishDetailsUseCase {
    override fun runCase(accessToken: String, fishId: Long?): Mono<List<FishDetails>> {
        return userService.getUserWithPropertyByAccessToken(accessToken)
            .flatMap { user ->
                userService.getAllFish(user.userId)
                    .filter {
                        it.fishId == fishId
                    }
                    .flatMap {
                        fishService.getFishDetail(it.fishId)
                    }
                    .collectList()
            }
            .defaultIfEmpty(listOf())

    }
}