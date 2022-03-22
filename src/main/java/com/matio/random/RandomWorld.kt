package com.matio.random

import cn.hutool.core.util.RandomUtil
import com.matio.random.domain.entity.Human
import com.matio.random.infra.JFunction
import com.matio.random.infra.config.ApplicationProperties
import com.matio.random.infra.handler.TaskHandler
import com.matio.random.infra.handler.WorldMessageDispatchHandler
import com.matio.random.infra.subscription.SubscriptionRegistry
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties::class)
open class RandomWorld(
    private val subscriptionRegistry: SubscriptionRegistry,
    private val worldMessageDispatchHandler: WorldMessageDispatchHandler,
    private val taskHandler: TaskHandler,
    private val properties: ApplicationProperties,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        val list = listOf("咕咕", "嘎嘎嘎", "咪咪", "喵喵", "xxx", "A", "Z", "999", "TAT", "###")
        list.forEachIndexed { i, name ->
            val human = Human(
                (i + 1).toLong(),
                name,
                1,
                properties = properties,
                sound = worldMessageDispatchHandler.worldChannel,
                taskChannel = taskHandler.taskHandler,
                vision = { subscriptionRegistry.findHumanByZone(it) }
            )
            subscriptionRegistry.subscribe(human, "world")
        }
//        for (i in 0..10) {
//            val human = Human(
//                1,
//                ,
//                1,
//                1000,
//                sound = worldMessageDispatchHandler.worldChannel,
//                taskChannel = taskHandler.taskHandler,
//                vision = { subscriptionRegistry.findHumanByZone(it) }
//            )
//            subscriptionRegistry.subscribe(human, "world")
//        }
//        val human = Human(
//            1,
//            "咕咕",
//            1,
//            1000,
//            sound = worldMessageDispatchHandler.worldChannel,
//            taskChannel = taskHandler.taskHandler,
//            vision = { subscriptionRegistry.findHumanByZone(it) }
//        )
//        subscriptionRegistry.subscribe(human, "world")
//        val human2 = Human(
//            2,
//            "嘎嘎",
//            1,
//            1000,
//            sound = worldMessageDispatchHandler.worldChannel,
//            taskChannel = taskHandler.taskHandler,
//            vision = { subscriptionRegistry.findHumanByZone(it) }
//        )
//        subscriptionRegistry.subscribe(human2, "world")
//        objectPool.add(human)
    }

}


fun main(args: Array<String>) {
    runApplication<RandomWorld>(*args)
}
