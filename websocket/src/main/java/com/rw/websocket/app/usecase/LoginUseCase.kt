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
        return userService.getUserByUserName(userName)
            .flatMap {
                userService.updateUserInfoCache(it.id)
            }
//            .flatMap {
//                if (SecureUtil.md5(password) != it.password) {
//                    Mono.error(PasswordWrongException())
//                } else {
//                    val accessToken = SecureUtil.md5(snowflake.nextId().toString())
//                    val userId = it.id
//                    userService.updateAccessToken(userId, accessToken)
//                }
//            }
    }
}
