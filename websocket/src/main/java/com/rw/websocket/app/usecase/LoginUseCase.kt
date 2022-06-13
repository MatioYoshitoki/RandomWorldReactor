package com.rw.websocket.app.usecase

import com.rw.websocket.app.service.UserService
import com.rw.websocket.domain.entity.UserWithProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface LoginUseCase {

    fun runCase(userName: String): Mono<UserWithProperty>

}

@Component
open class LoginUseCaseImpl(
    private val userService: UserService,
) : LoginUseCase {
    override fun runCase(userName: String): Mono<UserWithProperty> {
        return userService.updateUserInfoCache(userName)
    }
}
