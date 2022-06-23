package com.rw.random.infra.scheduler

import com.rw.random.app.usecase.DisasterUseCase
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
open class DisasterScheduler(
    private val disasterUseCase: DisasterUseCase
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 30_000, initialDelay = 3_000)
    @Async
    open fun run() {
        log.info("start disaster!")
        disasterUseCase.runCase()
    }

}
