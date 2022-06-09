package com.rw.websocket.app.usecase

import com.rw.random.common.dto.RWResult
import com.rw.websocket.app.service.FishService
import com.rw.websocket.domain.dto.request.FishRequest
import com.rw.websocket.infre.config.ApplicationProperties
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.net.URI

interface FishingUseCase {

    fun runCase(userId: Long, fishId: Long): Mono<Long>

}

@Component
open class FishingUseCaseImpl(
    private val applicationProperties: ApplicationProperties,
    private val fishService: FishService
) : FishingUseCase {

    private val webClient = WebClient.create()

    override fun runCase(userId: Long, fishId: Long): Mono<Long> {
        return fishService.checkFishOwner(fishId, userId)
            .filter { it }
            .flatMap {
                requestCoreObjOut(fishId)
                    .map {
                        it.data as Long
                    }
            }
    }

    private fun requestCoreObjOut(fishId: Long): Mono<RWResult<*>> {
        return webClient.post()
            .uri(URI.create(applicationProperties.coreUrl + "/api/v1/object/out"))
            .bodyValue(FishRequest(fishId))
            .retrieve()
            .bodyToMono(RWResult::class.java)
            .filter { it.errno == 0 }
            .filter { it.data != null }
    }
}
