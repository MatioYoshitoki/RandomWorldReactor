package com.rw.random.app.usecase

import cn.hutool.core.lang.Snowflake
import cn.hutool.core.util.RandomUtil
import com.rw.random.domain.entity.RWPersonality
import com.rw.random.domain.entity.RWZone
import com.rw.random.domain.entity.obj.Fish
import com.rw.random.domain.service.PersistenceService
import com.rw.random.infra.config.ApplicationProperties
import com.rw.random.infra.config.TaskProperties
import com.rw.random.infra.handler.TaskHandler
import com.rw.random.infra.handler.WorldMessageDispatchHandler
import com.rw.random.infra.utils.RandomName
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

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
    private val persistenceService: PersistenceService,
) : HelloWorldUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun runCase() {
        persistenceService.loadAllAliveFish()
            .then(Mono.just(1).doOnNext {
                for (i in 0 until applicationProperties.initFishCount) {
                    random()
                }
            })
            .subscribe()
//        applicationProperties.objects.forEach {
//            val name = it.key
//            log.info("init $name")
//
//            val fish = Fish(
//                snowflake.nextId(),
//                name,
//                taskProperties = taskProperties,
//                sound = worldMessageDispatchHandler.worldChannel,
//                taskChannel = taskHandler.taskHandler,
//                personality = RWPersonality.random(RandomUtil.randomInt(1, 196))
//            )
//            zone.enterZone(fish)
//        }
    }

    private fun random() {
        val count = zone.getAllObjByType(Fish::class).count()
        log.info("当前数量: $count, 目标数量：${applicationProperties.loadTestFishCount}")
        if (count >= applicationProperties.loadTestFishCount) {
            return
        }
        val randomLength = RandomUtil.randomInt(2, 4)
        val randomPersonalityIdx = RandomUtil.randomInt(1, 196)
        val fish = Fish(
            snowflake.nextId(),
            RandomName.randomName(true, randomLength),
            taskProperties = taskProperties,
            sound = worldMessageDispatchHandler.worldChannel,
            taskChannel = taskHandler.taskHandler,
            personality = RWPersonality.random(randomPersonalityIdx, applicationProperties.personalityName[randomPersonalityIdx])
        )
        zone.enterZone(fish)
    }
}
