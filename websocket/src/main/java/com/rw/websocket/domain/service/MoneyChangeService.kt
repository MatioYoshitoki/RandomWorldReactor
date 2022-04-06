package com.rw.websocket.domain.service

import com.rw.websocket.domain.entity.UserWithProperty
import com.rw.websocket.domain.repository.UserPropertyRepository
import com.rw.websocket.domain.repository.UserRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.concurrent.locks.ReentrantLock

interface MoneyChangeService {

    fun expendMoney(userId: Long, money: Long): Mono<Boolean>

    fun earnMoney(userId: Long, money: Long): Mono<Boolean>

}

@Service
open class MoneyChangeServiceImpl(
    private val userPropertyRepository: UserPropertyRepository,
    private val userRepository: UserRepository
) : MoneyChangeService {

    private val lock: ReentrantLock = ReentrantLock()

    override fun expendMoney(userId: Long, money: Long): Mono<Boolean> {
        return userPropertyRepository.findOne(userId)
            .filter {
                it.money!! >= money
            }
            .delayUntil {
                userPropertyRepository.updateMoney(userId, it.money!! - money)
            }
            .flatMap {
                userRepository.findAccessTokenByUserId(userId)
                    .flatMap { accessToken ->
                        userRepository.updateUserWithProperty(
                            accessToken,
                            mapOf(UserWithProperty.MONEY_FIELD to (it.money!! - money).toString())
                        )
                    }
            }
            .defaultIfEmpty(false)

    }

    override fun earnMoney(userId: Long, money: Long): Mono<Boolean> {
        return userPropertyRepository.findOne(userId)
            .delayUntil {
                userPropertyRepository.updateMoney(userId, it.money!! + money)
            }
            .flatMap {
                userRepository.findAccessTokenByUserId(userId)
                    .flatMap { accessToken ->
                        userRepository.updateUserWithProperty(
                            accessToken,
                            mapOf(UserWithProperty.MONEY_FIELD to (it.money!! + money).toString())
                        )
                    }
            }
    }
}