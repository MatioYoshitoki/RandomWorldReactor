package com.rw.random.app.usecase

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
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface EnterObjectUseCase {

    fun runCase(): Mono<Long>

}

@Component
open class EnterObjectUseCaseImpl(
    private val worldMessageDispatchHandler: WorldMessageDispatchHandler,
    private val taskHandler: TaskHandler,
    private val taskProperties: TaskProperties,
    private val zone: RWZone,
    private val snowflake: Snowflake,
) : EnterObjectUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun runCase(): Mono<Long> {
        return Mono.just(RandomName.randomName(true, 2))
            .doOnNext { log.info("init $it") }
            .map { randomFish(it) }
            .flatMap {
                if (zone.enterZone(it)) {
                    Mono.just(it.id)
                } else {
                    Mono.empty()
                }
            }
    }

    private fun randomFish(name: String): Fish {
        return Fish(
            snowflake.nextId(),
            name,
            hasMaster = true,
            taskProperties = taskProperties,
            sound = worldMessageDispatchHandler.worldChannel,
            taskChannel = taskHandler.taskHandler,
            personality = RWPersonality.random(RandomUtil.randomInt(1, 196))
        )
    }
}