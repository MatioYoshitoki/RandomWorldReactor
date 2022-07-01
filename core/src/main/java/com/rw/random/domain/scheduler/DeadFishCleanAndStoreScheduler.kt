package com.rw.random.domain.scheduler

import com.rw.random.domain.service.PersistenceService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
open class DeadFishCleanAndStoreScheduler(
    private val persistenceService: PersistenceService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 30_000, initialDelay = 30_000)
    fun doTask() {
        log.debug("清理无主死鱼")
        persistenceService.cleanFish()
            .subscribe()
    }

}
