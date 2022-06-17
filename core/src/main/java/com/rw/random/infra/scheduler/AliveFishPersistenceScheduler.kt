package com.rw.random.infra.scheduler

import cn.hutool.json.JSONUtil
import com.rw.random.domain.entity.RWZone
import com.rw.random.domain.entity.obj.Fish
import com.rw.random.domain.service.PersistenceService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
open class AliveFishPersistenceScheduler(
    private val zone: RWZone,
    private val persistenceService: PersistenceService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 500, initialDelay = 3_000)
    fun doTask() {
        log.debug("获取存活列表")
        Flux.fromStream(zone.getAllObjByType(Fish::class))
            .filter { it is Fish }
            .delayUntil {
                persistenceService.persistenceFish(it as Fish)
            }
            .onErrorResume {
                log.error("persistence error!", it)
                Mono.empty()
            }
            .subscribe()
    }
}
