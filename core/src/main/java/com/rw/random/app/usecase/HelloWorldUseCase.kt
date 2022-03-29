package com.rw.random.app.usecase

import cn.hutool.core.lang.Snowflake
import cn.hutool.core.util.RandomUtil
import com.rw.random.domain.entity.RWPersonality
import com.rw.random.domain.entity.RWZone
import com.rw.random.domain.entity.obj.Fish
import com.rw.random.infra.config.ApplicationProperties
import com.rw.random.infra.config.TaskProperties
import com.rw.random.infra.handler.TaskHandler
import com.rw.random.infra.handler.WorldMessageDispatchHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

interface HelloWorldUseCase {

    fun runCase()

}

@Component
open class HelloWorldUseCaseImpl(
    private val worldMessageDispatchHandler: WorldMessageDispatchHandler,
    private val taskHandler: TaskHandler,
    private val taskProperties: TaskProperties,
    private val zone: RWZone,
    private val applicationProperties: ApplicationProperties,
    private val snowflake: Snowflake,
) : HelloWorldUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun runCase() {
        applicationProperties.objects.forEach {
            val name = it.key
            log.info("init $name")

            val fish = Fish(
                snowflake.nextId(),
                name,
                taskProperties = taskProperties,
                sound = worldMessageDispatchHandler.worldChannel,
                taskChannel = taskHandler.taskHandler,
                personality = RWPersonality.random(RandomUtil.randomInt(1, 196))
            )
            zone.enterZone(fish)
        }
    }

}