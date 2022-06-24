package com.rw.websocket.api.controller

import com.rw.random.common.dto.RWResult
import com.rw.websocket.app.usecase.BuyFishUseCase
import com.rw.websocket.app.usecase.SellFishUseCase
import com.rw.websocket.app.usecase.SoldOutFishUseCase
import com.rw.websocket.app.usecase.ViewFishMarketUseCase
import com.rw.websocket.domain.dto.request.FishPriceRequest
import com.rw.websocket.domain.dto.request.FishRequest
import com.rw.websocket.domain.dto.request.FishSellListRequest
import com.rw.websocket.domain.dto.request.PageRequest
import com.rw.websocket.domain.entity.FishSellLog
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
        @RequestBody fishRequest: FishRequest
    ): Mono<RWResult<String>> {
        // TODO 买鱼
        return Mono.empty()
    }

    @PostMapping("/sell")
    fun fishSell(
        @RequestBody fishPriceRequest: FishPriceRequest
    ): Mono<RWResult<String>> {
        // TODO 卖鱼
        return Mono.empty()
    }

    @PostMapping("/cancel/sell")
    fun fishCancelSell(
        @RequestBody fishRequest: FishRequest
    ): Mono<RWResult<String>> {
        // TODO 取消卖鱼
        return Mono.empty()
    }

    @PostMapping("/change/price")
    fun fishChangePrice(
        @RequestBody fishPriceRequest: FishPriceRequest
    ): Mono<RWResult<String>> {
        // TODO 更改鱼价
        return Mono.empty()
    }

}
