package com.rw.websocket.app.usecase

import com.rw.websocket.app.service.UserService
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface LogoutUseCase {

    fun runCase(accessToken: String): Mono<Void>

}

@Component
open class LogoutUseCaseImpl(
    private val userService: UserService
) : LogoutUseCase {
    override fun runCase(accessToken: String): Mono<Void> {
        return userService.logout(accessToken)
    }
}