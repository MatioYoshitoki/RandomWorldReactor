package com.rw.websocket.app.usecase

import cn.hutool.crypto.SecureUtil
import com.rw.websocket.app.service.UserService
import com.rw.websocket.domain.dto.request.RegisterRequest
import com.rw.websocket.domain.entity.UserWithProperty
import com.rw.websocket.infre.exception.RegisterException
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface RegisterUseCase {

    fun runCase(request: RegisterRequest): Mono<UserWithProperty>

}

@Component
open class RegisterUseCaseImpl(
    private val userService: UserService,
) : RegisterUseCase {
    override fun runCase(request: RegisterRequest): Mono<UserWithProperty> {
        return Mono.just(request)
            .flatMap {
                if (it.password != it.passwordAgain) {
                    Mono.error(RegisterException("两次输入密码不一致"))
                } else {
                    Mono.just(it)
                }
            }
            .flatMap {
                userService.newUser(it.userName, SecureUtil.md5(it.password))
            }

    }


}