package com.rw.random.app.usecase

import com.rw.random.common.constants.BeingStatus
import com.rw.random.common.exception.PutObjectFailedException
import com.rw.random.domain.entity.RWZone
import com.rw.random.domain.service.PersistenceService
import com.rw.random.domain.service.UserFishService
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Month

interface PutObjectUseCase {

    fun runCase(fishId: Long): Mono<Long>

}

@Component
open class PutObjectUseCaseImpl(
    private val persistenceService: PersistenceService,
    private val zone: RWZone,
    private val userFishService: UserFishService
) : PutObjectUseCase {

    override fun runCase(fishId: Long): Mono<Long> {
        return persistenceService.loadFish(fishId)
            .filter { it.isSleep() }
            .filterWhen { userFishService.changeFishStatusToAlive(it) }
            .map {
                it.status = BeingStatus.ALIVE
                it
            }
            .publishOn(Schedulers.boundedElastic())
            .flatMap {
                if (!zone.enterZone(it)) {
                    userFishService.changeFishStatusToSleep(it)
                        .flatMap {
                            Mono.error(PutObjectFailedException())
                        }
                } else {
                    Mono.just(it.id)
                }
            }
    }

}