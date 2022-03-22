package com.matio.random.infra.scheduler

import com.matio.random.infra.subscription.SubscriptionRegistry
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
open class AliveHumanScheduler(
    private val subscriptionRegistry: SubscriptionRegistry
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 1_000, initialDelay = 10_000)
    fun doTask() {
        subscriptionRegistry.findHumanByZone(1)
            .forEach {
                log.info(
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