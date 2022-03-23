package com.matio.random.infra.scheduler

import com.matio.random.domain.entity.RWZone
import com.matio.random.domain.entity.TimeEvent
import com.matio.random.infra.handler.WorldMessageDispatchHandler
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
open class RandomMessageScheduler(
    private val worldMessageDispatchHandler: WorldMessageDispatchHandler,
    private val zone: RWZone
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 1_000, initialDelay = 3_000)
    fun doTask() {
        log.info("send message to ${zone.getZoneTopic()}")
        worldMessageDispatchHandler.sendMsg(TimeEvent(System.currentTimeMillis(), "TIMER", zone.getZoneTopic()))
    }
}