package com.rw.websocket.api.controller

import com.rw.random.common.dto.RWResult
import com.rw.websocket.app.usecase.LoginUseCase
import com.rw.websocket.domain.dto.request.LoginRequest
import com.rw.websocket.domain.dto.request.RegisterRequest
import com.rw.websocket.domain.entity.UserWithProperty
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/user")
open class UserController(
    private val loginUseCase: LoginUseCase
) {

    @PostMapping("/register")
    fun register(@RequestBody registerRequest: RegisterRequest): Mono<RWResult<String>> {
        // TODO 注册
        return Mono.empty()
    }

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): Mono<RWResult<UserWithProperty>> {
        return loginUseCase.runCase(loginRequest.userName, loginRequest.password)
            .map {
                RWResult.success("", it)
            }
//            .onErrorResume {
//                Mono.justOrEmpty(it.message?.let { it1 -> RWResult.failed(it1, "") })
//            }
//            .defaultIfEmpty(RWResult.failed("未知错误", ""))
    }

    @PostMapping("/logout")
    fun logout(@RequestHeader("access_token") accessToken: String): Mono<RWResult<String>> {
        // TODO 登出
        return Mono.empty()
    }

    @PostMapping("/signIn")
    fun signIn(@RequestHeader("access_token") accessToken: String): Mono<RWResult<String>> {
        // TODO 签到
        return Mono.empty()
    }


    @PostMapping("/package/expand")
    fun expandPackage(@RequestHeader("access_token") accessToken: String): Mono<RWResult<String>> {
        // TODO 鱼位扩展
        return Mono.empty()
    }


}