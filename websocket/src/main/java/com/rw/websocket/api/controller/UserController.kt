package com.rw.websocket.api.controller

import com.rw.random.common.dto.RWResult
import com.rw.websocket.app.usecase.LoginUseCase
import com.rw.websocket.domain.dto.request.LoginRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/user")
open class UserController(
    private val loginUseCase: LoginUseCase
) {

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): Mono<RWResult<String>> {
        return loginUseCase.runCase(loginRequest.userName, loginRequest.password)
            .map {
                RWResult.success("", it)
            }
            .onErrorResume {
                Mono.justOrEmpty(it.message?.let { it1 -> RWResult.failed(it1, "") })
            }
            .defaultIfEmpty(RWResult.failed("未知错误", ""))
    }

}