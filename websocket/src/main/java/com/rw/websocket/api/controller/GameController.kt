package com.rw.websocket.api.controller

import com.rw.random.common.dto.RWResult
import com.rw.websocket.app.service.GameService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/user/game")
open class GameController(
    private val gameService: GameService
) {

    @PostMapping("/fish/enter")
    fun fishEnter(@RequestHeader("access_token") accessToken: String): Mono<RWResult<Long>> {
        return gameService.enterFish(accessToken)
            .map {
                RWResult.success("投放成功!", it)
            }
            .defaultIfEmpty(RWResult.failed("投放失败", null))
    }

}