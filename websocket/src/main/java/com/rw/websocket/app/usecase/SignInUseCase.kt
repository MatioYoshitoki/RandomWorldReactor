package com.rw.websocket.app.usecase

import com.rw.websocket.app.service.UserService
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface SignInUseCase {

    fun runCase(accessToken: String): Mono<Boolean>

}

@Component
open class SignInUseCaseImpl(
    private val userService: UserService
) : SignInUseCase {

    override fun runCase(accessToken: String): Mono<Boolean> {
        return userService.signIn(accessToken)
    }
}