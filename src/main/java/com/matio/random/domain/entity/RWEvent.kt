package com.matio.random.domain.entity

abstract class RWEvent(
    val eventId: Long,
    val eventType: String,
    val topic: String,
    val source: RWObject?,
    val target: RWObject?,
) {
    var msg: String = ""
    override fun toString(): String {
        return "source=${source?.name}, target=${target?.name}, msg=$msg, type=${this.javaClass}"
    }
}


open class TimeEvent(
    eventId: Long,
    eventType: String,
    topic: String,
) : RWEvent(eventId, eventType, topic, null, null) {
}


open class ATKEvent(
    eventId: Long,
    eventType: String,
    val success: Boolean,
    val damage: Int,
    topic: String,
    source: RWObject,
    target: RWObject,
) : RWEvent(eventId, eventType, topic, source, target) {
    init {
        val result = if (success) {
            "造成伤害: ${damage}。"
        } else {
            "但是未命中。"
        }
        msg = "${source.name}(${source.id})对${target.name}(${target.id})发起了攻击。${result}"
    }
}

open class StayEvent(
    eventId: Long,
    eventType: String,
    topic: String,
    source: RWObject,
) : RWEvent(eventId, eventType, topic, source, null) {
    init {
        msg = "${source.name}(${source.id})停留原地。"
    }
}

open class EarnEvent(
    eventId: Long,
    eventType: String,
    val amount: Int,
    topic: String,
    target: RWObject,
) : RWEvent(eventId, eventType, topic, null, target) {
    init {
        msg = "${target.name}(${target.id})挖取金币: ${amount}。"
    }
}

open class UpgradeWeaponEvent(
    eventId: Long,
    eventType: String,
    topic: String,
    val atk: Int,
    target: RWObject
) : RWEvent(eventId, eventType, topic, null, target) {
    init {
        msg = "${target.name}(${target.id})升级武器！提升攻击力: $atk"
    }
}

open class UpgradeToolEvent(
    eventId: Long,
    eventType: String,
    topic: String,
    val earnSpeed: Int,
    target: RWObject,
) : RWEvent(eventId, eventType, topic, null, target) {
    init {
        msg = "${target.name}(${target.id})升级工具！提升挖矿效率: $earnSpeed"
    }
}

open class HealEvent(
    eventId: Long,
    eventType: String,
    topic: String,
    val heal: Int,
    target: RWObject,
) : RWEvent(eventId, eventType, topic, null, target) {
    init {
        msg = "${target.name}(${target.id})治疗自己！提升生命值: $heal"
    }
}

open class MoveEvent(
    eventId: Long,
    eventType: String,
    topic: String,
    source: RWObject,
) : RWEvent(eventId, eventType, topic, source, null) {
    init {
        msg = "${source.name}(${source.id})正在移动。"
    }
}

open class EnterZoneEvent(
    eventId: Long,
    eventType: String,
    private val zone: RWZone,
    topic: String,
    source: RWObject,
) : RWEvent(eventId, eventType, topic, source, null) {
    init {
        msg = "${source.name}(${source.id})进入区域${zone.zoneName}(${zone.zoneId})。"
    }
}

open class SystemEvent(
    eventId: Long,
    eventType: String,
    topic: String,
    source: RWObject?,
    target: RWObject?,
) : RWEvent(eventId, eventType, topic, source, target)


open class ObjectDestroyEvent(
    eventId: Long,
    eventType: String,
    topic: String,
    source: RWObject,
) : SystemEvent(eventId, eventType, topic, source, null) {
    init {
        msg = "${source.name}(${source.id})被销毁！"
    }
}