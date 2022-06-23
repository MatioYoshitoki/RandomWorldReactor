package com.rw.random.app.usecase

import com.rw.random.domain.entity.DisasterMessageEvent
import com.rw.random.domain.entity.RWZone
import com.rw.random.domain.entity.obj.Fish
import com.rw.random.infra.disaster.ThunderDisaster
import com.rw.random.infra.handler.WorldMessageDispatchHandler
import org.springframework.stereotype.Component
import kotlin.math.log

interface DisasterUseCase {

    fun runCase()

}

@Component
open class DisasterUseCaseImpl(
    private val thunderDisaster: ThunderDisaster,
    private val worldMessageDispatchHandler: WorldMessageDispatchHandler,
    private val zone: RWZone
) : DisasterUseCase {
    override fun runCase() {
        worldMessageDispatchHandler.sendMsg(DisasterMessageEvent(999999, "/topic/world", "Thunder"))
        zone.getAllObjByType(Fish::class)
            .forEach {
                thunderDisaster.suffer(it as Fish)
            }
    }
}
