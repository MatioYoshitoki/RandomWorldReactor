package com.rw.random.domain.entity

import com.rw.random.domain.entity.obj.ChaosGod

open class DisasterEvent(
    eventId: Long,
    topic: String,
    target: RWObject,
    god: ChaosGod
) : InternalEvent(eventId, "disaster", topic, god, target)

open class CrazyDisasterEvent(
    eventId: Long,
    val personality: CrazyPersonality,
    target: RWObject,
    god: ChaosGod
) : DisasterEvent(eventId, "disaster", target, god)


open class ThunderDisasterEvent(
    eventId: Long,
    target: RWObject,
    damage: Long,
    god: ChaosGod
) : DisasterEvent(eventId, "disaster", target, god)
