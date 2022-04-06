package com.rw.websocket.api.controller

import com.rw.random.common.dto.RWResult
import com.rw.websocket.app.service.GameService
import com.rw.websocket.app.usecase.FishCreateUseCase
import com.rw.websocket.domain.dto.request.FishRequest
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/game/fish")
open class FishController(
    private val fishCreateUseCase: FishCreateUseCase
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping("/list")
    fun fishList(
        @RequestHeader("access_token") accessToken: String
    ): Mono<RWResult<String>> {
        // TODO 鱼列表
        return Mono.empty()
    }

    @GetMapping("/details/{fishId}")
    fun fishDetail(
        @RequestHeader("access_token") accessToken: String,
        @PathVariable("fishId") fishId: Long,
    ): Mono<RWResult<String>> {
        // TODO 非池中鱼详情
        return Mono.empty()
    }

    @PostMapping("/put")
    fun fishPut(
        @RequestHeader("access_token") accessToken: String,
        @RequestBody fishRequest: FishRequest
    ): Mono<RWResult<String>> {
        log.info("fishId=${fishRequest.fishId}")
        // TODO 放鱼
        return Mono.empty()
    }


    @PostMapping("/create")
    fun fishCreate(@RequestHeader("access_token") accessToken: String): Mono<RWResult<Long>> {
        return fishCreateUseCase.runCase(accessToken)
            .map {
                RWResult.success("投放成功!", it)
            }
            .defaultIfEmpty(RWResult.failed("投放失败", null))
    }


    @PostMapping("/fishing")
    fun fishing(
        @RequestHeader("access_token") accessToken: String,
        @RequestBody fishRequest: FishRequest
    ): Mono<RWResult<String>> {
        // TODO 捞鱼
        return Mono.empty()
    }

    @PostMapping("/eat")
    fun fishEat(
        @RequestHeader("access_token") accessToken: String,
        @RequestBody fishRequest: FishRequest
    ): Mono<RWResult<String>> {
        // TODO 吃鱼
        return Mono.empty()
    }

}