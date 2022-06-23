package com.rw.random.infra.disaster

import com.rw.random.domain.entity.CrazyDisasterEvent
import com.rw.random.domain.entity.CrazyPersonality
import com.rw.random.domain.entity.obj.ChaosGod
import com.rw.random.domain.entity.obj.Fish
import com.rw.random.infra.handler.WorldMessageDispatchHandler
import org.springframework.stereotype.Component

@Component("crazyDisaster")
open class CrazyDisaster(
    private val worldMessageDispatchHandler: WorldMessageDispatchHandler,
    private val crazyGod: ChaosGod
) : Disaster {
    override fun survive(fish: Fish): Boolean {
        if (fish.isAlive() && fish.weight >= 5000000) {
            return true
        }
        return false
    }

    override fun suffer(fish: Fish) {
        if (survive(fish)) {
            worldMessageDispatchHandler.sendMsg(CrazyDisasterEvent(999999, CrazyPersonality(), fish, crazyGod))
        }
    }
}
