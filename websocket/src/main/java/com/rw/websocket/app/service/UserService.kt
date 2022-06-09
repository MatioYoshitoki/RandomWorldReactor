package com.rw.websocket.app.service

import cn.hutool.core.date.DatePattern
import com.rw.random.common.entity.UserFish
import com.rw.websocket.domain.entity.User
import com.rw.websocket.domain.entity.UserWithProperty
import com.rw.websocket.domain.repository.FishRepository
import com.rw.websocket.domain.repository.UserFishRepository
import com.rw.websocket.domain.repository.UserPropertyRepository
import com.rw.websocket.domain.repository.UserRepository
import com.rw.websocket.domain.repository.redis.AccessTokenUserRepository
import com.rw.websocket.domain.repository.redis.UserAccessTokenRepository
import com.rw.websocket.domain.repository.redis.UserInfoRepository
import com.rw.websocket.domain.repository.redis.UserSignInRepository
import com.rw.websocket.infre.exception.RegisterException
import com.rw.websocket.infre.utils.FishWeight2ExpUtil.exchangeWeight2Exp
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty
import java.util.*

interface UserService {

    fun eatFish(fishId: Long, userId: Long): Mono<Boolean>

    fun updateUserInfoCache(userId: Long): Mono<UserWithProperty>

    fun getUserWithPropertyByAccessToken(accessToken: String): Mono<UserWithProperty>

    fun getUserWithPropertyByUserId(userId: Long): Mono<UserWithProperty>

    fun getUserByUserName(userName: String): Mono<User>

    fun newUser(userName: String, password: String): Mono<Void>

    fun getAllFish(userId: Long): Flux<UserFish>

    fun bindUserFish(userId: Long, fishId: Long): Mono<UserFish>

    fun getUserFish(fishId: Long): Mono<UserFish>

    fun logout(accessToken: String): Mono<Void>

    fun signIn(userName: String): Mono<Boolean>

}

@Component
open class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userPropertyRepository: UserPropertyRepository,
    private val userAccessTokenRepository: UserAccessTokenRepository,
    private val accessTokenUserRepository: AccessTokenUserRepository,
    private val userInfoRepository: UserInfoRepository,
    private val userFishRepository: UserFishRepository,
    private val fishRepository: FishRepository,
    private val userSignInRepository: UserSignInRepository
) : UserService {
    override fun eatFish(fishId: Long, userId: Long): Mono<Boolean> {
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
                userInfoRepository.addAll(userId, mapOf("exp" to it.toString()))
            }
    }

    override fun updateUserInfoCache(userId: Long): Mono<UserWithProperty> {
        return userRepository.findUserWithPropertyFromDB(userId)
            .delayUntil {
                userInfoRepository.addAll(userId, it)
            }
            .map {
                UserWithProperty(
                    userId,
                    it[UserWithProperty.USER_NAME_FIELD]!!,
                    it[UserWithProperty.EXP_FIELD]?.toLong() ?: 0,
                    it[UserWithProperty.MONEY_FIELD]?.toLong() ?: 0,
                    it[UserWithProperty.FISH_MAX_COUNT_FIELD]?.toLong() ?: 0
                )
            }
    }

    override fun getUserWithPropertyByAccessToken(accessToken: String): Mono<UserWithProperty> {
        return accessTokenUserRepository.findOneUserProperty(accessToken)
    }

    override fun getUserWithPropertyByUserId(userId: Long): Mono<UserWithProperty> {
        return userInfoRepository.findOneUserProperty(userId)
    }

    override fun getUserByUserName(userName: String): Mono<User> {
        return userRepository.findOneByUserName(userName)
    }

    override fun newUser(userName: String, password: String): Mono<Void> {
        return userNameExist(userName)
            .flatMap { exist ->
                if (exist) {
                    Mono.error(RegisterException("用户名被占用"))
                } else {
                    Mono.just(exist)
                }
            }
            .flatMap {
                userRepository.addOne(userName, password)
            }
            .flatMap {
                userPropertyRepository.addOne(it.id)
            }
//            .flatMap {
//                updateUserInfoCache(it.id!!)
//            }
//            .flatMap {
//                val accessToken = SecureUtil.md5(snowflake.nextId().toString())
//                val userId = it.id!!
//                updateAccessToken(userId, accessToken)
//            }
            .switchIfEmpty { Mono.error(RegisterException("用户名被占用")) }
            .then()
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

    override fun logout(accessToken: String): Mono<Void> {
        return accessTokenUserRepository.findOneUserProperty(accessToken)
            .flatMap {
                accessTokenUserRepository.removeOne(accessToken)
                    .then(userAccessTokenRepository.removeOne(it.userId))
            }
    }

    override fun signIn(userName: String): Mono<Boolean> {
        val date = DatePattern.PURE_DATE_FORMAT.format(Date())
        return userSignInRepository.exist(userName, date)
            .flatMap { exist ->
                if (!exist) {
                    userRepository.findOneByUserName(userName)
                        .flatMap {
                            val userId = it.id
                            userPropertyRepository.findOne(userId)
                                .flatMap { property ->
                                    userPropertyRepository.updateMoney(userId, property.money!! + 5000)
                                        .flatMap {
                                            userInfoRepository.addAll(
                                                userId,
                                                mapOf("money" to (property.money!! + 5000).toString())
                                            )
                                        }
                                }
                                .delayUntil {
                                    userSignInRepository.addOne(userName, date)
                                }
                        }

                } else {
                    Mono.just(false)
                }
            }
    }

    private fun userNameExist(userName: String): Mono<Boolean> {
        return userRepository.findOneByUserName(userName)
            .map {
                true
            }
            .defaultIfEmpty(false)
    }
}
