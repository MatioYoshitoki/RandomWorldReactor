package com.rw.random.app.usecase

import com.rw.random.domain.entity.RWZone
import com.rw.random.domain.entity.obj.Fish
import com.rw.random.infra.subscription.SubscriptionRegistry
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono


interface OutObjectUseCase {

    fun runCase(fishId: Long): Mono<Long>

}

@Component
open class OutObjectUseCaseImpl(
    private val subscriptionRegistry: SubscriptionRegistry,
    private val zone: RWZone
) : OutObjectUseCase {

    override fun runCase(fishId: Long): Mono<Long> {

        return zone.getAllObjByType(Fish::class)
            .filter { it.id == fishId }
            .filter {  }
    }
}