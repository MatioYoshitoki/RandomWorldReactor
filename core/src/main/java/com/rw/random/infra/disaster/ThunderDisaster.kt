package com.rw.random.infra.disaster

import cn.hutool.core.util.RandomUtil
import com.rw.random.domain.entity.ATKEvent
import com.rw.random.domain.entity.BeAtkEvent
import com.rw.random.domain.entity.CrazyDisasterEvent
import com.rw.random.domain.entity.CrazyPersonality
import com.rw.random.domain.entity.obj.ChaosGod
import com.rw.random.domain.entity.obj.Fish
import com.rw.random.infra.handler.WorldMessageDispatchHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("thunderDisaster")
open class ThunderDisaster(
    private val worldMessageDispatchHandler: WorldMessageDispatchHandler,
    private val thunderGod: ChaosGod
) : Disaster {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun survive(fish: Fish): Boolean {
        if (fish.isAlive() && fish.weight >= 5000000) {
            return true
        }
        return false
    }

    override fun suffer(fish: Fish) {
        if (survive(fish)) {
            val rate = RandomUtil.randomBoolean()
            var damage = fish.maxHeal * 0.8
            if (rate) {
                damage = fish.maxHeal * 1.2
            }
            worldMessageDispatchHandler.sendMsg(
                ATKEvent(
                    999999,
                    damage.toInt(),
                    fish.topic,
                    thunderGod,
                    fish,
                )
            )
        }
    }
}
