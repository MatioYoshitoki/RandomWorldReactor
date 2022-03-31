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
}
