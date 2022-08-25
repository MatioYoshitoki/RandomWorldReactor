package com.rw.websocket.app.usecase

import com.rw.random.common.dto.RWResult
import com.rw.websocket.app.service.FishService
import com.rw.websocket.domain.dto.request.FishRequest
import com.rw.websocket.infre.config.ApplicationProperties
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.net.URI

interface PutFishUseCase {

    fun runCase(userName: String, fishId: Long): Mono<String>

}

@Component
open class PutFishUseCaseImpl(
    private val applicationProperties: ApplicationProperties,
    private val fishService: FishService
) : PutFishUseCase {

    private val webClient = WebClient.create()

    override fun runCase(userName: String, fishId: Long): Mono<String> {
        return fishService.checkFishOwner(fishId, userName)
            .filter { it }
            .flatMap {
                requestCoreObjOut(fishId)
                    .map {
                        (it.data as Long).toString()
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
