package com.matio.random.app.usecase

import cn.hutool.core.lang.Snowflake
import cn.hutool.core.util.StrUtil
import com.matio.random.domain.entity.Human
import com.matio.random.domain.entity.RWZone
import com.matio.random.infra.config.ObjectProperties
import com.matio.random.infra.config.TaskProperties
import com.matio.random.infra.config.ZoneProperties
import com.matio.random.infra.handler.TaskHandler
import com.matio.random.infra.handler.WorldMessageDispatchHandler
import com.matio.random.infra.utils.RandomName
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface EnterHumanUseCase {

    fun runCase(): Mono<Boolean>

}

@Component
open class EnterHumanUseCaseImpl(
    private val worldMessageDispatchHandler: WorldMessageDispatchHandler,
    private val taskHandler: TaskHandler,
    private val zoneProperties: ZoneProperties,
    private val taskProperties: TaskProperties,
    private val zone: RWZone,
    private val snowflake: Snowflake,
) : EnterHumanUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun runCase(): Mono<Boolean> {
        return Mono.just(RandomName.randomName(true, 2))
            .doOnNext { log.info("init $it") }
            .map { randomHuman(it) }
            .map { zone.enterZone(it) }
    }

    private fun randomHuman(name: String): Human {
        return Human(
            snowflake.nextId(),
            name,
            zoneProperties = zoneProperties,
            taskProperties = taskProperties,
            sound = worldMessageDispatchHandler.worldChannel,
            taskChannel = taskHandler.taskHandler,
        )
    }
}