package com.rw.random.domain.service

import com.rw.random.common.constants.BeingStatus
import com.rw.random.domain.entity.obj.Fish
import com.rw.random.domain.repository.UserFishRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

interface UserFishService {
    fun changeFishStatusToSleep(fish: Fish): Mono<Boolean>
    fun changeFishStatusToAlive(fish: Fish): Mono<Boolean>
    fun changeFishStatusToDead(fish: Fish): Mono<Boolean>
}

@Service
open class UserFishServiceImpl(
    private val userFishRepository: UserFishRepository
) : UserFishService {
    override fun changeFishStatusToSleep(fish: Fish): Mono<Boolean> {
        return Mono.just(fish)
            .flatMap {
                if (fish.isAlive()) {
                    userFishRepository.updateStatus(fish.id, BeingStatus.SLEEP)
                        .thenReturn(true)
                } else {
                    Mono.just(false)
                }
            }
    }

    override fun changeFishStatusToAlive(fish: Fish): Mono<Boolean> {
        return Mono.just(fish)
            .flatMap {
                if (fish.isSleep()) {
                    userFishRepository.updateStatus(fish.id, BeingStatus.ALIVE)
                        .thenReturn(true)
                } else {
                    Mono.just(false)
                }
            }
    }

    override fun changeFishStatusToDead(fish: Fish): Mono<Boolean> {
        return Mono.just(fish)
            .flatMap {
                userFishRepository.updateStatus(fish.id, BeingStatus.DEAD)
                    .thenReturn(true)
            }
    }
}
