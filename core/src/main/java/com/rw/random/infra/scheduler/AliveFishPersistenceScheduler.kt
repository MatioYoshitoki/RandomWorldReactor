package com.rw.random.infra.scheduler

import com.rw.random.domain.entity.RWZone
import com.rw.random.domain.entity.obj.Fish
import com.rw.random.domain.repository.FishRepository
import com.rw.random.domain.service.PersistenceService
import com.rw.random.infra.subscription.SubscriptionRegistry
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
open class AliveFishPersistenceScheduler(
    private val subscriptionRegistry: SubscriptionRegistry,
    private val zone: RWZone,
    private val persistenceService: PersistenceService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 500, initialDelay = 3_000)
    fun doTask() {
        log.info("获取存活列表")
        Flux.fromStream(subscriptionRegistry.findObjByTopic(zone.getZoneTopic(), Fish::class))
            .filter { it is Fish }
            .delayUntil {
                persistenceService.persistenceFish(it as Fish)
            }
            .subscribe()
    }
}