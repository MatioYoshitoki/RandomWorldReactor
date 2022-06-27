package com.rw.websocket.api.controller

import com.rw.random.common.dto.RWResult
import com.rw.websocket.app.usecase.BuyFishUseCase
import com.rw.websocket.app.usecase.SellFishUseCase
import com.rw.websocket.app.usecase.SoldOutFishUseCase
import com.rw.websocket.app.usecase.ViewFishMarketUseCase
import com.rw.websocket.domain.dto.request.*
import com.rw.websocket.domain.entity.FishSellLog
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/game/fish/market")
open class FishMarketController(
    private val viewFishMarketUseCase: ViewFishMarketUseCase,
    private val sellFishUseCase: SellFishUseCase,
    private val soldOutFishUseCase: SoldOutFishUseCase,
    private val buyFishUseCase: BuyFishUseCase
) {


    @GetMapping("/list")
    fun fishMarketList(
        @RequestBody pageRequest: FishSellListRequest
    ): Mono<RWResult<List<FishSellLog>>> {
        // TODO 鱼市列表
        return viewFishMarketUseCase.runCase(
            pageRequest.orderId,
            pageRequest.orderBy,
            pageRequest.pageNo,
            pageRequest.pageSize
        )
            .collectList()
            .map { RWResult.success("success", it) }
            .onErrorResume { Mono.just(RWResult.failed("failed", null)) }
    }

    @PostMapping("/buy")
    fun fishBuy(
        @AuthenticationPrincipal principal: Mono<UserDetails>,
        @RequestBody fishOrderRequest: FishOrderRequest
    ): Mono<RWResult<FishSellLog>> {
        // TODO 买鱼
        return principal
            .flatMap { buyFishUseCase.runCase(it.username, fishOrderRequest.orderId) }
            .map { RWResult.success("success", it) }
            .defaultIfEmpty(RWResult.failed("failed", null))
            .onErrorResume { Mono.just(RWResult.failed("failed", null)) }
    }

    @PostMapping("/sell")
    fun fishSell(
        @AuthenticationPrincipal principal: Mono<UserDetails>,
        @RequestBody fishPriceRequest: FishPriceRequest
    ): Mono<RWResult<Nothing>> {
        // TODO 卖鱼
        return principal
            .delayUntil {
                sellFishUseCase.runCase(it.username, fishPriceRequest.fishId, fishPriceRequest.price)
            }
            .map { RWResult.success("success", null) }
            .defaultIfEmpty(RWResult.failed("failed", null))
            .onErrorResume { Mono.just(RWResult.failed("failed", null)) }
    }

    @PostMapping("/sold/out")
    fun fishSoldOut(
        @AuthenticationPrincipal principal: Mono<UserDetails>,
        @RequestBody fishOrderRequest: FishOrderRequest
    ): Mono<RWResult<Nothing>> {
        // TODO 取消卖鱼
        return principal
            .delayUntil {
                soldOutFishUseCase.runCase(it.username, fishOrderRequest.orderId)
            }
            .map { RWResult.success("success", null) }
            .defaultIfEmpty(RWResult.failed("failed", null))
            .onErrorResume { Mono.just(RWResult.failed("failed", null)) }
    }

//    @PostMapping("/change/price")
//    fun fishChangePrice(
//        @RequestBody fishPriceRequest: FishPriceRequest
//    ): Mono<RWResult<String>> {
//        // TODO 更改鱼价
//        return Mono.empty()
//    }

}
