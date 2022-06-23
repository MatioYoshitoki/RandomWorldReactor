package com.rw.random.domain.entity

open class WorldMessageEvent(eventId: Long, topic: String) : RWEvent(eventId, "worldMessage", topic, null, null)


open class DisasterMessageEvent(
    eventId: Long,
    topic: String,
    val disasterType: String
) : WorldMessageEvent(eventId, topic) {
    override fun toString(): String {
        return """
        {"event_type": "$eventType", "disasterType": "$disasterType"}
        """.trimIndent()
    }
}
