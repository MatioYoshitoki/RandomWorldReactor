package com.rw.websocket.api.controller

import com.rw.random.common.dto.RWResult
import com.rw.websocket.app.usecase.LoginUseCase
import com.rw.websocket.app.usecase.LogoutUseCase
import com.rw.websocket.app.usecase.RegisterUseCase
import com.rw.websocket.app.usecase.SignInUseCase
import com.rw.websocket.domain.dto.request.LoginRequest
import com.rw.websocket.domain.dto.request.RegisterRequest
import com.rw.websocket.domain.entity.UserWithProperty
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/user")
open class UserController(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val signInUseCase: SignInUseCase
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/register")
    fun register(@RequestBody registerRequest: RegisterRequest): Mono<RWResult<UserWithProperty>> {
        // TODO 注册
        return registerUseCase.runCase(registerRequest)
            .map {
                RWResult.success("success", it)
            }
            .onErrorResume {
                log.error("register error", it)
                Mono.just(RWResult.failed(it.message ?: "", null))
            }
            .defaultIfEmpty(RWResult.failed("未知错误", null))
    }

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): Mono<RWResult<UserWithProperty>> {
        return loginUseCase.runCase(loginRequest.userName, loginRequest.password)
            .map {
                RWResult.success("success", it)
            }
            .onErrorResume {
                log.error("login error", it)
                Mono.just(RWResult.failed(it.message ?: "", null))
            }
            .defaultIfEmpty(RWResult.failed("未知错误", null))
    }

    @PostMapping("/logout")
    fun logout(@RequestHeader("access_token") accessToken: String): Mono<RWResult<Void>> {
        // 登出
        return logoutUseCase.runCase(accessToken)
            .thenReturn(RWResult.success("成功", null))
    }

    @PostMapping("/signIn")
    fun signIn(@RequestHeader("access_token") accessToken: String): Mono<RWResult<Boolean>> {
        // 签到
        return signInUseCase.runCase(accessToken)
            .map {
                if (it) {
                    RWResult.success("签到成功", it)
                } else {
                    RWResult.failed("今日已签到", it)
                }
            }
            .defaultIfEmpty(RWResult.failed("今日已签到", false))
            .onErrorResume {
                log.error("sign in error", it)
                Mono.just(RWResult.failed("今日已签到", false))
            }
    }


    @PostMapping("/package/expand")
    fun expandPackage(@RequestHeader("access_token") accessToken: String): Mono<RWResult<String>> {
        // TODO 鱼位扩展
        return Mono.empty()
    }


}