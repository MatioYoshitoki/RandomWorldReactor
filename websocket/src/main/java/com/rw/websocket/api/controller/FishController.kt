package com.rw.websocket.api.controller

import com.rw.random.common.dto.RWResult
import com.rw.websocket.app.service.GameService
import com.rw.websocket.app.usecase.*
import com.rw.websocket.domain.dto.request.FishDetails
import com.rw.websocket.domain.dto.request.FishRequest
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/game/fish")
open class FishController(
    private val fishCreateUseCase: FishCreateUseCase,
    private val fishingUseCase: FishingUseCase,
    private val fishPutFishUseCase: PutFishUseCase,
    private val eatFishUseCase: EatFishUseCase,
    private val fishDetailsUseCase: FishDetailsUseCase
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping("/list")
    fun fishDetail(
        @RequestHeader("access_token") accessToken: String,
        @RequestParam("fishId") fishId: Long?,
    ): Mono<RWResult<List<FishDetails>>> {
        // 鱼列表
        return fishDetailsUseCase.runCase(accessToken, fishId)
            .map {
                RWResult.success("成功", it)
            }
            .defaultIfEmpty(RWResult.failed("获取失败", null))
    }

    @PostMapping("/put")
    fun fishPut(
        @RequestHeader("access_token") accessToken: String,
        @RequestBody fishRequest: FishRequest
    ): Mono<RWResult<Long>> {
        log.info("fishId=${fishRequest.fishId}")
        // 放鱼
        return fishPutFishUseCase.runCase(accessToken, fishRequest.fishId)
            .map {
                RWResult.success("投放成功", it)
            }
            .onErrorResume {
                log.error("create error", it)
                Mono.just(RWResult.failed("投放失败, ${it.message}", null))
            }
            .defaultIfEmpty(RWResult.failed("投放失败", null))
    }


    @PostMapping("/create")
    fun fishCreate(@RequestHeader("access_token") accessToken: String): Mono<RWResult<Long>> {
        return fishCreateUseCase.runCase(accessToken)
            .map {
                RWResult.success("投放成功!", it)
            }
            .onErrorResume {
                log.error("create error", it)
                Mono.just(RWResult.failed("投放失败, ${it.message}", null))
            }
            .defaultIfEmpty(RWResult.failed("投放失败", null))
    }


    @PostMapping("/fishing")
    fun fishing(
        @RequestHeader("access_token") accessToken: String,
        @RequestBody fishRequest: FishRequest
    ): Mono<RWResult<Long>> {
        // 捞鱼
        return fishingUseCase.runCase(accessToken, fishRequest.fishId)
            .map { RWResult.success("捕捞成功", it) }
            .defaultIfEmpty(RWResult.failed("捕捞失败", null))
    }

    @PostMapping("/eat")
    fun fishEat(
        @RequestHeader("access_token") accessToken: String,
        @RequestBody fishRequest: FishRequest
    ): Mono<RWResult<Nothing>> {
        // 吃鱼
        return eatFishUseCase.runCase(accessToken, fishRequest.fishId)
            .filter { it }
            .map {
                RWResult.success("成功", null)
            }
            .onErrorResume {
                log.error("eat error", it)
                Mono.just(RWResult.failed("失败", null))
            }
            .defaultIfEmpty(RWResult.failed("失败", null))
    }

}