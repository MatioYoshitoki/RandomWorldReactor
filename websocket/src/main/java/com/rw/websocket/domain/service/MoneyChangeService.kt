package com.rw.websocket.domain.service

import com.rw.websocket.domain.repository.UserPropertyRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.concurrent.locks.ReentrantLock

interface MoneyChangeService {

    fun expendMoney(userId: Long, money: Long): Mono<Boolean>

    fun earnMoney(userId: Long, money: Long): Mono<Boolean>

}

@Service
open class MoneyChangeServiceImpl(
    private val userPropertyRepository: UserPropertyRepository
): MoneyChangeService {

    private val lock: ReentrantLock = ReentrantLock()

    override fun expendMoney(userId: Long, money: Long): Mono<Boolean> {
        return userPropertyRepository.findOne(userId)
            .filter {
                it.money!! >= money
            }
            .flatMap{
                userPropertyRepository.updateMoney(userId, it.money!! - money)
            }
            .defaultIfEmpty(false)

    }

    override fun earnMoney(userId: Long, money: Long): Mono<Boolean> {
        return userPropertyRepository.findOne(userId)
            .flatMap {
                userPropertyRepository.updateMoney(userId, it.money!! + money)
            }
    }
}