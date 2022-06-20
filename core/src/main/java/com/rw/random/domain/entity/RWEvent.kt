package com.rw.random.domain.entity

import com.rw.random.infra.constants.GrowthType

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
    target: RWObject?,
) : SystemEvent(eventId, eventType, topic, source, target) {
    init {
        msg = "${source.name} 被销毁！"
    }

    override fun toString(): String {
        return """
        {"source_name": "${source?.name}", "source_id": ${source?.id}, "target_name": "${target?.name}", "target_id": ${target?.id}, "event_type": "$eventType"}
        """.trimIndent()
    }
}

// 养鱼所需

open class TimeEvent(
    eventId: Long,
    eventType: String,
    topic: String,
) : RWEvent(eventId, eventType, topic, null, null)

open class ATKEvent(
    eventId: Long,
    eventType: String,
    val atk: Int,
    topic: String,
    source: RWObject,
    target: RWObject,
) : RWEvent(eventId, eventType, topic, source, target)

open class StayEvent(
    eventId: Long,
    eventType: String,
    topic: String,
    source: RWObject,
) : RWEvent(eventId, eventType, topic, source, null)

open class EarnEvent(
    eventId: Long,
    eventType: String,
    val amount: Long,
    topic: String,
    target: RWObject,
) : RWEvent(eventId, eventType, topic, null, target)

open class GrowthEvent(
    eventId: Long,
    eventType: String,
    topic: String,
    target: RWObject,
    val growthType: GrowthType,
    val growthValue: Int,
) : RWEvent(eventId, eventType, topic, null, target)

open class InternalEvent(
    eventId: Long,
    eventType: String,
    topic: String,
    source: RWObject,
    target: RWObject
) : RWEvent(eventId, eventType, topic, source, target)

open class BeAtkEvent(
    eventId: Long,
    eventType: String,
    topic: String,
    source: RWObject,
    target: RWObject,
    success: Boolean,
    val ct: Boolean,
    val damage: Int,
) : InternalEvent(eventId, eventType, topic, source, target) {
    init {
        msg = if (success) {
            "${source.name} 受到【${target.name}】攻击,${if (ct) "产生暴击，" else ""} 生命值减少${damage}。"
        } else {
            "${source.name} 受到【${target.name}】攻击, 但未命中。"
        }
    }

    override fun toString(): String {
        return """
        {"source_name": "${source?.name}", "source_id": ${source?.id}, "target_name": "${target?.name}", "target_id": ${target?.id}, "event_type": "$eventType", "damage": $damage, "ct": $ct}
        """.trimIndent()
    }


}
