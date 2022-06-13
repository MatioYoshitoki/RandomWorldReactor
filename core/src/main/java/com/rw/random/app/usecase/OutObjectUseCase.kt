package com.rw.random.app.usecase

import com.rw.random.common.constants.BeingStatus
import com.rw.random.domain.entity.RWZone
import com.rw.random.domain.entity.obj.Fish
import com.rw.random.domain.service.PersistenceService
import com.rw.random.domain.service.UserFishService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono


interface OutObjectUseCase {

    fun runCase(fishId: Long): Mono<Long>

}

@Component
open class OutObjectUseCaseImpl(
    private val persistenceService: PersistenceService,
    private val zone: RWZone,
    private val userFishService: UserFishService
) : OutObjectUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun runCase(fishId: Long): Mono<Long> {
        val fishOption = zone.getAllObjByType(Fish::class)
            .map { it as Fish }
            .filter { it.id == fishId }
            .filter { it.status == BeingStatus.ALIVE }
            .findFirst()
        return Mono.justOrEmpty(fishOption)
            .doOnNext {
                zone.clearObj(it)
            }
            .filterWhen {
                userFishService.changeFishStatusToSleep(it)
            }
            .map {
                it.status = BeingStatus.SLEEP
                it
            }
            .delayUntil {
                persistenceService.persistenceFish(it)
            }
            .map {
                it.id
            }
            .onErrorResume {
                log.error("out object error! ", it)
                Mono.empty()
            }
    }
}
