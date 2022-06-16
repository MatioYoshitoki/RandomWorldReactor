package com.rw.random.common.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class RedisStreamMessage(
//    @JsonProperty("source_name")
//    var sourceName: String? = null,
//    @JsonProperty("source_id")
//    var sourceId: Long? = null,
//    @JsonProperty("target_name")
//    var targetName: String? = null,
//    @JsonProperty("target_id")
//    var targetId: Long? = null,
//    @JsonProperty("event_type")
//    var eventType: String? = null,
//    @JsonProperty("level")
//    var level: Int? = null,
//    @JsonProperty("message")
//    var message: String? = null,
    @JsonProperty("dest")
    var dest: String? = null,
    @JsonProperty("__PAYLOAD")
    var payload: Any? = null
) {

    override fun toString(): String {
        return """
            {"dest": "$dest", "__PAYLOAD": $payload}
        """.trimIndent()
    }
}
