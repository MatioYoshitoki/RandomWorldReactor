package com.matio.random.domain.entity

abstract class RWZone {
    val zoneId: Long = 0
    val zoneName: String = ""
    val neighbor: List<RWZone> = listOf()
}