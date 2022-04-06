package com.rw.websocket.app.usecase

import cn.hutool.core.lang.Snowflake
import cn.hutool.crypto.SecureUtil
import com.rw.websocket.domain.entity.UserWithProperty
import com.rw.websocket.domain.repository.UserRepository
import com.rw.websocket.infre.exception.PasswordWrongException
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface LoginUseCase {

    fun runCase(userName: String, password: String): Mono<UserWithProperty>

}

@Component
open class LoginUseCaseImpl(
    private val userRepository: UserRepository,
    private val snowflake: Snowflake,
) : LoginUseCase {
    override fun runCase(userName: String, password: String): Mono<UserWithProperty> {
        return userRepository.findOneByUserName(userName)
            .flatMap {
                if (SecureUtil.md5(password) != it.password) {
                    Mono.error(PasswordWrongException())
                } else {
                    val accessToken = SecureUtil.md5(snowflake.nextId().toString())
                    val userId = it.id
                    userRepository.updateAccessToken(userId, accessToken)
                }
            }
    }
}