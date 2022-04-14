package com.rw.random.common.dto

data class RedisStreamMessage(
    var sourceName: String?,
    var sourceId: Long?,
    var targetName: String?,
    var targetId: Long?,
    var eventType: String?,
    var level: Int?,
    var message: String?
) {
    constructor() : this(null, null, null, null, null, null, null)

    override fun toString(): String {
        return """
            {"source_name": "$sourceName", "source_id": $sourceId, "target_name": "$targetName", "target_id": $targetId, "event_type": "$eventType", "level": $level, "message": "$message"}
        """.trimIndent()
    }
}
