package com.matio.random.infra.manager

import com.matio.random.domain.entity.Human
import com.matio.random.domain.entity.RWObject
import com.matio.random.infra.handler.WorldMessageDispatchHandler
import com.matio.random.infra.subscription.SubscriptionRegistry
import org.springframework.context.SmartLifecycle

//@Component
open class RandomObjectManager(
    private val subscriptionRegistry: SubscriptionRegistry,
    private val worldMessageDispatchHandler: WorldMessageDispatchHandler,
) : SmartLifecycle {
    private val objectPool = mutableSetOf<RWObject>()

    private fun createObj() {
//        val human = Human(1, "testUser", 1, sound = worldMessageDispatchHandler.worldChannel, taskChannel = null)
//        subscriptionRegistry.subscribe(human, "world")
//        objectPool.add(human)
    }

    override fun start() {
        createObj()
    }

    override fun stop() {
    }

    override fun isRunning(): Boolean {
        return true
    }
}