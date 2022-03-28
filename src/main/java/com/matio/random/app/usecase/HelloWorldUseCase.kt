package com.matio.random.app.usecase

import cn.hutool.core.lang.Snowflake
import cn.hutool.core.util.RandomUtil
import com.matio.random.domain.entity.*
import com.matio.random.domain.entity.obj.Fish
import com.matio.random.infra.config.ObjectProperties
import com.matio.random.infra.config.TaskProperties
import com.matio.random.infra.handler.TaskHandler
import com.matio.random.infra.handler.WorldMessageDispatchHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

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
                personality = RWPersonality.random(RandomUtil.randomInt(1, 196))
            )
            zone.enterZone(fish)
        }
    }

}