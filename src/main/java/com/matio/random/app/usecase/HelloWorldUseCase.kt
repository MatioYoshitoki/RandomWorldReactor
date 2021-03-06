package com.matio.random.app.usecase

import cn.hutool.core.lang.Snowflake
import com.matio.random.domain.entity.Human
import com.matio.random.domain.entity.RWZone
import com.matio.random.infra.config.ObjectProperties
import com.matio.random.infra.config.TaskProperties
import com.matio.random.infra.config.ZoneProperties
import com.matio.random.infra.handler.TaskHandler
import com.matio.random.infra.handler.WorldMessageDispatchHandler
import com.matio.random.infra.subscription.SubscriptionRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

interface HelloWorldUseCase {

    fun runCase()

}

@Component
open class HelloWorldUseCaseImpl(
    private val worldMessageDispatchHandler: WorldMessageDispatchHandler,
    private val taskHandler: TaskHandler,
    private val zoneProperties: ZoneProperties,
    private val taskProperties: TaskProperties,
    private val zone: RWZone,
    private val objectProperties: ObjectProperties,
    private val snowflake: Snowflake,
) : HelloWorldUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun runCase() {
        objectProperties.objects.forEach {
            val name = it.key
            log.info("init $name")
            val human = Human(
                snowflake.nextId(),
                name,
                zoneProperties = zoneProperties,
                taskProperties = taskProperties,
                sound = worldMessageDispatchHandler.worldChannel,
                taskChannel = taskHandler.taskHandler,
            )
            zone.enterZone(human)
        }
    }

}