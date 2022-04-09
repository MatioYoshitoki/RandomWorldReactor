package com.rw.websocket.app.usecase

import com.rw.websocket.domain.dto.request.FishDetails
import com.rw.websocket.domain.repository.FishRepository
import com.rw.websocket.domain.repository.UserFishRepository
import com.rw.websocket.domain.repository.UserRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface FishDetailsUseCase {

    fun runCase(accessToken: String, fishId: Long?): Mono<List<FishDetails>>

}

@Component
open class FishDetailsUseCaseImpl(
    private val fishRepository: FishRepository,
    private val userRepository: UserRepository,
    private val userFishRepository: UserFishRepository
) : FishDetailsUseCase {
    override fun runCase(accessToken: String, fishId: Long?): Mono<List<FishDetails>> {
        return userRepository.findUserWithPropertyByToken(accessToken)
            .flatMap { user ->
                userFishRepository.findAll(user.userId)
                    .filter {
                        it.fishId == fishId
                    }
                    .flatMap {
                        fishRepository.findOne(it.fishId)
                    }
                    .collectList()
            }
            .defaultIfEmpty(listOf())

    }
}