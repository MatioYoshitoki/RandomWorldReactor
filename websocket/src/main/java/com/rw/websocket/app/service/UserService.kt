package com.rw.websocket.app.service

import cn.hutool.core.lang.Snowflake
import cn.hutool.crypto.SecureUtil
import com.rw.random.common.entity.UserFish
import com.rw.websocket.domain.entity.User
import com.rw.websocket.domain.entity.UserWithProperty
import com.rw.websocket.domain.repository.FishRepository
import com.rw.websocket.domain.repository.UserFishRepository
import com.rw.websocket.domain.repository.UserPropertyRepository
import com.rw.websocket.domain.repository.UserRepository
import com.rw.websocket.domain.repository.redis.AccessTokenUserRepository
import com.rw.websocket.domain.repository.redis.UserAccessTokenRepository
import com.rw.websocket.infre.exception.RegisterException
import com.rw.websocket.infre.utils.FishWeight2ExpUtil.exchangeWeight2Exp
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty

interface UserService {

    fun eatFish(fishId: Long, userId: Long, accessToken: String): Mono<Boolean>

    fun updateAccessToken(userId: Long, accessToken: String): Mono<UserWithProperty>

    fun getUserWithPropertyByAccessToken(accessToken: String): Mono<UserWithProperty>

    fun getUserByUserName(userName: String): Mono<User>

    fun newUser(userName: String, password: String): Mono<UserWithProperty>

    fun getAllFish(userId: Long): Flux<UserFish>

    fun bindUserFish(userId: Long, fishId: Long): Mono<UserFish>

    fun getUserFish(fishId: Long): Mono<UserFish>

}

@Component
open class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userPropertyRepository: UserPropertyRepository,
    private val userAccessTokenRepository: UserAccessTokenRepository,
    private val accessTokenUserRepository: AccessTokenUserRepository,
    private val userFishRepository: UserFishRepository,
    private val fishRepository: FishRepository,
    private val snowflake: Snowflake
) : UserService {
    override fun eatFish(fishId: Long, userId: Long, accessToken: String): Mono<Boolean> {
        return userPropertyRepository.findOne(userId)
            .flatMap { property ->
                fishRepository.findOne(fishId)
                    .map { exchangeWeight2Exp(it.weight) }
                    .filterWhen {
                        userPropertyRepository.updateExp(userId, property.exp ?: (0 + it))
                    }
                    .map {
                        property.exp ?: (0 + it)
                    }
            }
            .flatMap {
                accessTokenUserRepository.addAll(accessToken, mapOf("exp" to it.toString()))
            }
    }

    override fun updateAccessToken(userId: Long, accessToken: String): Mono<UserWithProperty> {
        return userRepository.updateAccessToken(userId, accessToken)
            .filter { it >= 1 }
            .delayUntil {
                userAccessTokenRepository.addOne(userId, accessToken)
            }
            .flatMap {
                userRepository.findUserWithPropertyFromDB(userId)
                    .delayUntil {
                        accessTokenUserRepository.addAll(accessToken, it)
                    }
                    .map {
                        UserWithProperty(
                            userId,
                            it[UserWithProperty.USER_NAME_FIELD]!!,
                            accessToken,
                            it[UserWithProperty.EXP_FIELD]?.toLong() ?: 0,
                            it[UserWithProperty.MONEY_FIELD]?.toLong() ?: 0,
                            it[UserWithProperty.FISH_MAX_COUNT_FIELD]?.toLong() ?: 0
                        )
                    }
            }
    }

    override fun getUserWithPropertyByAccessToken(accessToken: String): Mono<UserWithProperty> {
        return accessTokenUserRepository.findOneUserProperty(accessToken)
    }

    override fun getUserByUserName(userName: String): Mono<User> {
        return userRepository.findOneByUserName(userName)
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
                updateAccessToken(userId, accessToken)
            }
            .switchIfEmpty { Mono.error(RegisterException("用户名被占用")) }
    }

    override fun getAllFish(userId: Long): Flux<UserFish> {
        return userFishRepository.findAll(userId)
    }

    override fun bindUserFish(userId: Long, fishId: Long): Mono<UserFish> {
        return userFishRepository.addOne(userId, fishId)
    }

    override fun getUserFish(fishId: Long): Mono<UserFish> {
        return userFishRepository.findOne(fishId)
    }

    private fun userNameExist(userName: String): Mono<Boolean> {
        return userRepository.findOneByUserName(userName)
            .map {
                true
            }
            .defaultIfEmpty(false)
    }
}