package com.rw.websocket.api.controller

import com.rw.random.common.dto.RWResult
import com.rw.websocket.domain.dto.request.*
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/game/fish/market")
open class FishMarketController {


    @GetMapping("/list")
    fun fishMarketList(
        @RequestBody pageRequest: PageRequest
    ): Mono<RWResult<String>> {
        // TODO 鱼市列表
        return Mono.empty()
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
