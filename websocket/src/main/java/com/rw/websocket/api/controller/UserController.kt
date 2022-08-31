package com.rw.websocket.api.controller

import com.rw.random.common.dto.RWResult
import com.rw.random.common.security.TokenProvider
import com.rw.websocket.app.usecase.LoginUseCase
import com.rw.websocket.app.usecase.RegisterUseCase
import com.rw.websocket.app.usecase.SignInUseCase
import com.rw.websocket.domain.dto.request.LoginRequest
import com.rw.websocket.domain.dto.request.RegisterRequest
import com.rw.websocket.domain.entity.UserProperty
import com.rw.websocket.domain.entity.UserWithProperty
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/user")
open class UserController(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val signInUseCase: SignInUseCase,
    private val authenticationManager: ReactiveAuthenticationManager,
    private val tokenProvider: TokenProvider
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/register")
    fun register(@RequestBody registerRequest: RegisterRequest): Mono<RWResult<UserProperty>> {
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

        return Mono.just(1)
            .flatMap {
                authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken(
                        loginRequest.userName,
                        loginRequest.password
                    )
                )
            }
            .map { tokenProvider.createToken(it, true) }
            .flatMap { jwt ->
                loginUseCase.runCase(loginRequest.userName)
                    .map {
                        it.accessToken = jwt
                        it
                    }
            }
            .map {
                RWResult.success("success", it)
            }
            .onErrorResume {
                log.error("login error", it)
                Mono.just(RWResult.failed(it.message ?: "", null))
            }
            .defaultIfEmpty(RWResult.failed("未知错误", null))
    }

//    @PostMapping("/logout")
//    fun logout(@RequestHeader("access_token") accessToken: String): Mono<RWResult<Void>> {
//        // 登出
//        return logoutUseCase.runCase(accessToken)
//            .thenReturn(RWResult.success("成功", null))
//    }

    @PostMapping("/sign_in")
    fun signIn(@AuthenticationPrincipal principal: Mono<UserDetails>): Mono<RWResult<Boolean>> {
        // 签到
        return principal
            .flatMap {
                signInUseCase.runCase(it.username)
                    .map { result ->
                        if (result) {
                            RWResult.success("签到成功", result)
                        } else {
                            RWResult.failed("今日已签到", result)
                        }
                    }
                    .defaultIfEmpty(RWResult.failed("今日已签到", false))
                    .onErrorResume { err ->
                        log.error("sign in error", err)
                        Mono.just(RWResult.failed("今日已签到", false))
                    }
            }
            .defaultIfEmpty(RWResult.failed("用户未登录", null))
    }


    @PostMapping("/package/expand")
    fun expandPackage(): Mono<RWResult<String>> {
        // TODO 鱼位扩展
        return Mono.empty()
    }

}
