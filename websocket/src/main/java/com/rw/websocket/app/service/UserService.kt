package com.rw.websocket.app.service

import cn.hutool.core.lang.Snowflake
import cn.hutool.crypto.SecureUtil
import com.rw.websocket.domain.entity.UserWithProperty
import com.rw.websocket.domain.repository.UserPropertyRepository
import com.rw.websocket.domain.repository.UserRepository
import com.rw.websocket.infre.exception.RegisterException
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty
import javax.print.attribute.standard.JobOriginatingUserName

interface UserService {

    fun eatFish(fishId: Long, userId: Long, accessToken: String): Mono<Boolean>

    fun newUser(userName: String, password: String): Mono<UserWithProperty>
}

@Component
open class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userPropertyRepository: UserPropertyRepository,
    private val fishService: FishService,
    private val snowflake: Snowflake
) : UserService {
    override fun eatFish(fishId: Long, userId: Long, accessToken: String): Mono<Boolean> {
        return userPropertyRepository.findOne(userId)
            .flatMap { property ->
                fishService.fishExp(fishId)
                    .filterWhen {
                        userPropertyRepository.updateExp(userId, property.exp ?: (0 + it))
                    }
                    .map {
                        property.exp ?: (0 + it)
                    }
            }
            .flatMap {
                userRepository.updateUserWithProperty(accessToken, mapOf("exp" to it.toString()))
            }
    }

    override fun newUser(userName: String, password: String): Mono<UserWithProperty> {
        return userNameExist(userName)
            .flatMap { exist ->
                if (exist) {
                    Mono.error(RegisterException("用户名被占用"))
                } else {
                    Mono.just(exist)
                }
            }
            .flatMap {
                userRepository.addOne(userName, SecureUtil.md5(password))
            }
            .flatMap {
                userPropertyRepository.addOne(it.id)
            }
            .flatMap {
                val accessToken = SecureUtil.md5(snowflake.nextId().toString())
                val userId = it.id!!
                userRepository.updateAccessToken(userId, accessToken)
            }
            .switchIfEmpty { Mono.error(RegisterException("用户名被占用")) }
    }

    private fun userNameExist(userName: String): Mono<Boolean> {
        return userRepository.findOneByUserName(userName)
            .map {
                true
            }
            .defaultIfEmpty(false)
    }
}