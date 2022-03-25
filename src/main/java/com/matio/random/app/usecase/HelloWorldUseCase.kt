package com.matio.random.app.usecase

import cn.hutool.core.lang.Snowflake
import com.matio.random.domain.entity.*
import com.matio.random.domain.entity.obj.Fish
import com.matio.random.infra.config.ObjectProperties
import com.matio.random.infra.config.TaskProperties
import com.matio.random.infra.handler.TaskHandler
import com.matio.random.infra.handler.WorldMessageDispatchHandler
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
    private val objectProperties: ObjectProperties,
    private val snowflake: Snowflake,
) : HelloWorldUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun runCase() {
        objectProperties.objects.forEach {
            val name = it.key
            log.info("init $name")
            val fish = Fish(
                snowflake.nextId(),
                name,
                taskProperties = taskProperties,
                sound = worldMessageDispatchHandler.worldChannel,
                taskChannel = taskHandler.taskHandler,
                personality = RWPersonality(
                    mapOf(
                        ATKEvent::class to sortedMapOf(
                            500 to StayTask::class,
                            4000 to ATKTask::class,
                            10000 to EarnTask::class,
                        ),
                        EarnEvent::class to sortedMapOf(
                            800 to ATKTask::class,
                            2500 to StayTask::class,
                            10000 to EarnTask::class,
                        ),
                        TimeEvent::class to sortedMapOf(
                            300 to ATKTask::class,
                            1500 to StayTask::class,
                            10000 to EarnTask::class,
                        ),
                        GrowthEvent::class to sortedMapOf(
                            1000 to ATKTask::class,
                            1500 to StayTask::class,
                            10000 to EarnTask::class,
                        )
                    )
                )
            )
            zone.enterZone(fish)
        }
    }

}