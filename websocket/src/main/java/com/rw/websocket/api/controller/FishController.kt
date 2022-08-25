package com.rw.websocket.api.controller

import com.rw.random.common.dto.RWResult
import com.rw.websocket.app.usecase.*
import com.rw.websocket.domain.dto.request.FishDetails
import com.rw.websocket.domain.dto.request.FishRequest
import org.slf4j.LoggerFactory
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
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
        @AuthenticationPrincipal principal: Mono<UserDetails>,
        @RequestParam("fish_id") fishId: Long?,
    ): Mono<RWResult<List<FishDetails>>> {
        // 鱼列表
        return principal
            .flatMap {
                fishDetailsUseCase.runCase(it.username, fishId)
                    .map { result ->
                        RWResult.success("成功", result)
                    }
                    .defaultIfEmpty(RWResult.failed("获取失败", null))
            }
            .defaultIfEmpty(RWResult.failed("用户未登录", null))

    }

    @PostMapping("/put")
    fun fishPut(
        @AuthenticationPrincipal principal: Mono<UserDetails>,
        @RequestBody fishRequest: FishRequest
    ): Mono<RWResult<String>> {
        log.info("fishId=${fishRequest.fishId}")
        // 放鱼
        return principal
            .flatMap {
                fishPutFishUseCase.runCase(it.username, fishRequest.fishId)
                    .map { result ->
                        RWResult.success("投放成功", result)
                    }
                    .onErrorResume { err ->
                        log.error("create error", err)
                        Mono.just(RWResult.failed("投放失败, ${err.message}", null))
                    }
                    .defaultIfEmpty(RWResult.failed("投放失败", null))
            }
            .defaultIfEmpty(RWResult.failed("用户未登录", null))
    }


    @PostMapping("/create")
    fun fishCreate(
        @AuthenticationPrincipal principal: Mono<UserDetails>,
    ): Mono<RWResult<String>> {
        return principal
            .flatMap {
                fishCreateUseCase.runCase(it.username)
                    .map { result ->
                        RWResult.success("投放成功!", result)
                    }
                    .onErrorResume { err ->
                        log.error("create error", err)
                        Mono.just(RWResult.failed("投放失败, ${err.message}", null))
                    }
                    .defaultIfEmpty(RWResult.failed("投放失败", null))
            }
            .defaultIfEmpty(RWResult.failed("用户未登录", null))
    }


    @PostMapping("/fishing")
    fun fishing(
        @AuthenticationPrincipal principal: Mono<UserDetails>,
        @RequestBody fishRequest: FishRequest
    ): Mono<RWResult<String>> {
        // 捞鱼
        return principal
            .flatMap {
                fishingUseCase.runCase(it.username, fishRequest.fishId)
                    .map { result -> RWResult.success("捕捞成功", result) }
                    .defaultIfEmpty(RWResult.failed("捕捞失败", null))
            }
            .defaultIfEmpty(RWResult.failed("用户未登录", null))
    }

    @PostMapping("/eat")
    fun fishEat(
        @AuthenticationPrincipal principal: Mono<UserDetails>,
        @RequestBody fishRequest: FishRequest
    ): Mono<RWResult<Nothing>> {
        // 吃鱼
        return principal
            .flatMap {
                eatFishUseCase.runCase(it.username, fishRequest.fishId)
                    .filter { result -> result }
                    .map {
                        RWResult.success("成功", null)
                    }
                    .onErrorResume { err ->
                        log.error("eat error", err)
                        Mono.just(RWResult.failed("失败", null))
                    }
                    .defaultIfEmpty(RWResult.failed("失败", null))
            }
            .defaultIfEmpty(RWResult.failed("用户未登录", null))
    }

}
