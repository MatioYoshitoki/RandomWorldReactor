package com.rw.random.app.usecase

import com.rw.random.domain.entity.RWZone
import com.rw.random.domain.service.PersistenceService
import com.rw.random.domain.service.UserFishService
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

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
            .doOnNext { zone.enterZone(it) }
            .map { fishId }
    }

}