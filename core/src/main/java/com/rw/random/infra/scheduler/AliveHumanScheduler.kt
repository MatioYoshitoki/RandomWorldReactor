package com.rw.random.infra.scheduler

import com.rw.random.domain.entity.RWZone
import com.rw.random.domain.entity.obj.Fish
import com.rw.random.infra.subscription.SubscriptionRegistry
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
        subscriptionRegistry.findObjByTopic(zone.getZoneTopic(), Fish::class)
            .forEach {
                val fish = it as Fish
                println(
                    """
            -------------------------
            |编号  | ${fish.id}         
            |名称  | ${fish.name}    
            |性格  | ${fish.personality.personalityName()}
            |金币  | ${fish.money}
            |体重  | ${fish.weight}克
            |最大HP| ${fish.maxHeal}
            |HP   | ${fish.heal}
            |HP恢复| ${fish.recoverSpeed}   
            |攻击  | ${fish.atk}   
            |防御  | ${fish.def}   
            |进食  | ${fish.earnSpeed}  
            -------------------------
        """.trimIndent()
                )
            }

    }
}