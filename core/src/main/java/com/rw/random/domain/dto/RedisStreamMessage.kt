package com.rw.random.domain.dto

data class RedisStreamMessage(
    val sourceName: String,
    val sourceId: Long,
    val message: String
)
