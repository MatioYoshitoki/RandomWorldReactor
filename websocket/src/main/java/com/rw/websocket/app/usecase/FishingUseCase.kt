package com.rw.websocket.app.usecase

import com.fasterxml.jackson.databind.ObjectMapper
import com.rw.random.common.dto.RWResult
import com.rw.websocket.app.service.FishService
import com.rw.websocket.domain.dto.request.FishRequest
import com.rw.websocket.infra.config.ApplicationProperties
import com.rw.websocket.infra.event.MessageSendEvent
import com.rw.websocket.infra.event.MessageSendEventPayload
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationEventPublisherAware
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.net.URI

interface FishingUseCase {

    fun runCase(userName: String, fishId: Long): Mono<String>

}

@Component
open class FishingUseCaseImpl(
    private val applicationProperties: ApplicationProperties,
    private val fishService: FishService,
    private val objectMapper: ObjectMapper
) : FishingUseCase, ApplicationEventPublisherAware {

    private val webClient = WebClient.create()
    private lateinit var publisher: ApplicationEventPublisher

    override fun runCase(userName: String, fishId: Long): Mono<String> {
        return fishService.checkFishOwner(fishId, userName)
            .filter { it }
            .flatMap {
                requestCoreObjOut(fishId)
                    .map {
                        it.data as Long
                    }
            }
            .delayUntil {
                fishService.getFishDetail(it)
                    .doOnNext { fish ->
                        val payload = """
                            {"event_type": "fish_heartbeat", "fish_details": ${objectMapper.writeValueAsString(fish)}}
                        """.trimIndent()
                        val message = """
                            {"dest": "/topic/user/${fish.masterId}", "__PAYLOAD": $payload}
                        """.trimIndent()
                        publisher.publishEvent(MessageSendEvent(MessageSendEventPayload.of(message)))
                    }
            }
            .map { it.toString() }
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

    override fun setApplicationEventPublisher(applicationEventPublisher: ApplicationEventPublisher) {
        this.publisher = applicationEventPublisher
    }
}
