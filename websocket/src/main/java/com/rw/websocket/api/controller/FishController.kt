package com.rw.websocket.api.controller

import com.rw.random.common.dto.RWResult
import com.rw.random.common.utils.SecurityUtils
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
        @RequestParam("fishId") fishId: Long?,
    ): Mono<RWResult<List<FishDetails>>> {
        // 鱼列表
        return SecurityUtils.getCurrentUserLogin()
            .map {
                fishDetailsUseCase.runCase(it.toLong(), fishId)
                    .map { result ->
                        RWResult.success("成功", result)
                    }
                    .defaultIfEmpty(RWResult.failed("获取失败", null))
            }
            .orElseGet {
                Mono.just(RWResult.failed("用户未登录", null))
            }

    }

    @PostMapping("/put")
    fun fishPut(
        @RequestBody fishRequest: FishRequest
    ): Mono<RWResult<Long>> {
        log.info("fishId=${fishRequest.fishId}")
        // 放鱼
        return SecurityUtils.getCurrentUserLogin()
            .map {
                fishPutFishUseCase.runCase(it.toLong(), fishRequest.fishId)
                    .map { result ->
                        RWResult.success("投放成功", result)
                    }
                    .onErrorResume { err ->
                        log.error("create error", err)
                        Mono.just(RWResult.failed("投放失败, ${err.message}", null))
                    }
                    .defaultIfEmpty(RWResult.failed("投放失败", null))
            }
            .orElseGet {
                Mono.just(RWResult.failed("用户未登录", null))
            }
    }


    @PostMapping("/create")
    fun fishCreate(): Mono<RWResult<Long>> {
        return SecurityUtils.getCurrentUserLogin()
            .map {
                fishCreateUseCase.runCase(it.toLong())
                    .map { result ->
                        RWResult.success("投放成功!", result)
                    }
                    .onErrorResume { err ->
                        log.error("create error", err)
                        Mono.just(RWResult.failed("投放失败, ${err.message}", null))
                    }
                    .defaultIfEmpty(RWResult.failed("投放失败", null))
            }
            .orElseGet {
                Mono.just(RWResult.failed("用户未登录", null))
            }

    }


    @PostMapping("/fishing")
    fun fishing(@RequestBody fishRequest: FishRequest): Mono<RWResult<Long>> {
        // 捞鱼
        return SecurityUtils.getCurrentUserLogin().map {
            fishingUseCase.runCase(it.toLong(), fishRequest.fishId)
                .map { result -> RWResult.success("捕捞成功", result) }
                .defaultIfEmpty(RWResult.failed("捕捞失败", null))
        }
            .orElseGet {
                Mono.just(RWResult.failed("用户未登录", null))
            }
    }

    @PostMapping("/eat")
    fun fishEat(
        @RequestBody fishRequest: FishRequest
    ): Mono<RWResult<Nothing>> {
        // 吃鱼
        return SecurityUtils.getCurrentUserLogin()
            .map {
                eatFishUseCase.runCase(it.toLong(), fishRequest.fishId)
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

            .orElseGet { Mono.just(RWResult.failed("用户未登录", null)) }
    }

}
