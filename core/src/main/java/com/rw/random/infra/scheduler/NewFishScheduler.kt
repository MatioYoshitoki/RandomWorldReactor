package com.rw.random.infra.scheduler

import cn.hutool.core.lang.Snowflake
import cn.hutool.core.util.RandomUtil
import com.rw.random.domain.entity.RWPersonality
import com.rw.random.domain.entity.RWZone
import com.rw.random.domain.entity.obj.Fish
import com.rw.random.infra.config.TaskProperties
import com.rw.random.infra.handler.TaskHandler
import com.rw.random.infra.handler.WorldMessageDispatchHandler
import com.rw.random.infra.utils.RandomName
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
open class NewFishScheduler(
    private val worldMessageDispatchHandler: WorldMessageDispatchHandler,
    private val zone: RWZone,
    private val snowflake: Snowflake,
    private val taskHandler: TaskHandler,
    private val taskProperties: TaskProperties
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 1_000, initialDelay = 3_000)
    fun doTask() {
        val count = zone.getAllObjByType(Fish::class).count()
        log.info("当前数量: $count")
        if (count >= 100) {
            return
        }
        val randomLength = RandomUtil.randomInt(2, 4)
        val fish = Fish(
            snowflake.nextId(),
            RandomName.randomName(true, randomLength),
            taskProperties = taskProperties,
            sound = worldMessageDispatchHandler.worldChannel,
            taskChannel = taskHandler.taskHandler,
            personality = RWPersonality.random(RandomUtil.randomInt(1, 196))
        )
        zone.enterZone(fish)
    }
}