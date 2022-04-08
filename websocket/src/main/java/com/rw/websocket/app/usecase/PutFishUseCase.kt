package com.rw.websocket.app.usecase

import com.rw.random.common.dto.RWResult
import com.rw.websocket.domain.dto.request.FishRequest
import com.rw.websocket.domain.repository.UserFishRepository
import com.rw.websocket.domain.repository.UserRepository
import com.rw.websocket.infre.config.ApplicationProperties
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.net.URI

interface PutFishUseCase {

    fun runCase(accessToken: String, fishId: Long): Mono<Long>

}

@Component
open class PutFishUseCaseImpl(
    private val applicationProperties: ApplicationProperties,
    private val userRepository: UserRepository,
    private val userFishRepository: UserFishRepository
) : PutFishUseCase {

    private val webClient = WebClient.create()

    override fun runCase(accessToken: String, fishId: Long): Mono<Long> {
        return userRepository.findUserWithPropertyByToken(accessToken)
            .flatMap { user ->
                userFishRepository.findFishOwner(fishId)
                    .filter {
                        it == user.userId
                    }
            }
            .flatMap {
                requestCoreObjOut(fishId)
                    .map {
                        it.data as Long
                    }
            }
    }

    private fun requestCoreObjOut(fishId: Long): Mono<RWResult<*>> {
        return webClient.post()
            .uri(URI.create(applicationProperties.coreUrl + "/api/v1/object/put"))
            .bodyValue(FishRequest(fishId))
            .retrieve()
            .bodyToMono(RWResult::class.java)
            .filter { it.errno == 0 }
            .filter { it.data != null }
    }
}