package com.rw.websocket.app.service

import com.rw.random.common.dto.RWResult
import com.rw.websocket.domain.repository.UserFishRepository
import com.rw.websocket.domain.repository.UserRepository
import com.rw.websocket.infre.config.ApplicationProperties
import com.rw.websocket.infre.exception.EnterFishException
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
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
        return Mono.empty()
    }

    override fun fishing(): Mono<Void> {
        TODO("Not yet implemented")
    }
}