package com.rw.random.infra.scheduler

import com.rw.random.domain.entity.RWZone
import com.rw.random.domain.entity.TimeEvent
import com.rw.random.infra.handler.WorldMessageDispatchHandler
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
open class TimerMessageScheduler(
    private val worldMessageDispatchHandler: WorldMessageDispatchHandler,
    private val zone: RWZone
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 1_000, initialDelay = 3_000)
    fun doTask() {
        worldMessageDispatchHandler.sendMsg(TimeEvent(System.currentTimeMillis(), "TIMER", zone.getZoneTopic()))
    }
}