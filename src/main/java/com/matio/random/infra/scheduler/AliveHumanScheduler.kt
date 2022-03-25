package com.matio.random.infra.scheduler

import com.matio.random.domain.entity.RWZone
import com.matio.random.infra.subscription.SubscriptionRegistry
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
open class AliveHumanScheduler(
    private val subscriptionRegistry: SubscriptionRegistry,
    private val zone: RWZone
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 10_000, initialDelay = 10_000)
    fun doTask() {
        log.info("获取存活列表")
        subscriptionRegistry.findHumanByTopic(zone.getZoneTopic())
            .forEach {
                println(
                    """
            -------------------------
            |编号 | ${it.id}         
            |名称 | ${it.name}       
            |金币 | ${it.money}      
            |生命 | ${it.heal}       
            |攻击 | ${it.atk}        
            |挖矿 | ${it.earnSpeed}  
            -------------------------
        """.trimIndent()
                )
            }

    }
}