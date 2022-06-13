package com.rw.websocket.domain.service

import com.rw.websocket.app.service.UserService
import com.rw.websocket.domain.repository.UserPropertyRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.concurrent.locks.ReentrantLock

interface MoneyChangeService {

    fun expendMoney(userId: Long, userName: String, money: Long): Mono<Boolean>

    fun earnMoney(userId: Long, userName: String, money: Long): Mono<Boolean>

}

@Service
open class MoneyChangeServiceImpl(
    private val userPropertyRepository: UserPropertyRepository,
    private val userService: UserService
) : MoneyChangeService {

    private val lock: ReentrantLock = ReentrantLock()
    private val log = LoggerFactory.getLogger(javaClass)

    override fun expendMoney(userId: Long, userName: String, money: Long): Mono<Boolean> {
        return userPropertyRepository.findOne(userId)
            .filter {
                it.money!! >= money
            }
            .delayUntil {
                userPropertyRepository.updateMoney(userId, it.money!! - money)
            }
            .delayUntil {
                userService.updateUserInfoCache(userName)
            }
            .map { true }
            .defaultIfEmpty(false)
            .onErrorResume {
                log.error("expend money error!", it.message)
                Mono.just(false)
            }

    }

    override fun earnMoney(userId: Long, userName: String, money: Long): Mono<Boolean> {
        return userPropertyRepository.findOne(userId)
            .delayUntil {
                userPropertyRepository.updateMoney(userId, it.money!! + money)
            }
            .delayUntil {
                userService.updateUserInfoCache(userName)
            }
            .map { true }
            .defaultIfEmpty(false)
            .onErrorResume {
                log.error("earn money error!", it.message)
                Mono.just(false)
            }
    }
}
