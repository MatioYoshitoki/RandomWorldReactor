package com.rw.websocket.app.service

import com.rw.random.common.dto.RWResult
import com.rw.websocket.domain.repository.UserFishRepository
import com.rw.websocket.domain.repository.UserRepository
import com.rw.websocket.infre.config.ApplicationProperties
import com.rw.websocket.infre.exception.EnterFishException
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty
import java.net.URI

interface GameService {

    fun enterFish(accessToken: String): Mono<Long>

    fun fishing(): Mono<Void>

}

@Component
open class GameServiceImpl(
    private val applicationProperties: ApplicationProperties,
    private val userRepository: UserRepository,
    private val userFishRepository: UserFishRepository,
) : GameService {

    private val webClient = WebClient.create()

    override fun enterFish(accessToken: String): Mono<Long> {
        return userRepository.findIdByToken(accessToken)
            .flatMap { userId ->
                webClient.post()
                    .uri(URI.create(applicationProperties.coreUrl + "/v1/api/object"))
                    .retrieve()
                    .bodyToMono(RWResult::class.java)
                    .filter {
                        it.data != null
                    }
                    .flatMap {
                        if (it.errno == 0) {
                            val fishId = it.data.toString().toLong()
                            userFishRepository.bindFish(userId, fishId)
                                .map { fishId }
                        } else {
                            Mono.error(EnterFishException(it.message))
                        }
                    }
            }
    }

    override fun fishing(): Mono<Void> {
        TODO("Not yet implemented")
    }
}