package com.matio.random

import com.matio.random.domain.entity.Human
import com.matio.random.domain.entity.RWZone
import com.matio.random.infra.config.ApplicationProperties
import com.matio.random.infra.handler.TaskHandler
import com.matio.random.infra.handler.WorldMessageDispatchHandler
import com.matio.random.infra.subscription.SubscriptionRegistry
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
open class RandomWorld(
    private val subscriptionRegistry: SubscriptionRegistry,
    private val worldMessageDispatchHandler: WorldMessageDispatchHandler,
    private val taskHandler: TaskHandler,
    private val properties: ApplicationProperties,
    private val zone: RWZone,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        val list = listOf("咕咕", "嘎嘎嘎", "咪咪", "喵喵", "xxx", "A", "Z", "999", "TAT", "###")
        list.forEachIndexed { i, name ->
            val human = Human(
                (i + 1).toLong(),
                name,
                "",
                properties = properties,
                sound = worldMessageDispatchHandler.worldChannel,
                taskChannel = taskHandler.taskHandler,
                vision = { subscriptionRegistry.findHumanByTopic(it) }
            )
            zone.enterZone(human)
        }
    }

}


fun main(args: Array<String>) {
    runApplication<RandomWorld>(*args)
}
