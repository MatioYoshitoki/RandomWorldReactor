package com.rw.random.app.usecase

import com.rw.random.common.constants.BeingStatus
import com.rw.random.domain.entity.RWZone
import com.rw.random.domain.entity.obj.Fish
import com.rw.random.domain.repository.FishRepository
import com.rw.random.infra.subscription.SubscriptionRegistry
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono


interface OutObjectUseCase {

    fun runCase(fishId: Long): Mono<Long>

}

@Component
open class OutObjectUseCaseImpl(
    private val fishRepository: FishRepository,
    private val zone: RWZone,
) : OutObjectUseCase {

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
            .delayUntil {
                fishRepository.saveOne(it)
            }
            .map {
                it.id
            }
    }
}