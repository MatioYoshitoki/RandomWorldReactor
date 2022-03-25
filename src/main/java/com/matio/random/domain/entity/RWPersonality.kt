package com.matio.random.domain.entity

import java.util.TreeMap
import kotlin.reflect.KClass

class RWPersonality(
    private val eventBehavior: Map<KClass<RWEvent>, TreeMap<Int, KClass<out RWTask>>>
) {

    fun randomTask(event: RWEvent): KClass<out RWTask>? {
        val taskBehavior = eventBehavior[event::class]!!
        val rate: Int = (Math.random() * 10000).toInt()
        for (entry in taskBehavior) {
            if (rate <= entry.key) {
                return entry.value
            }
        }
        return null
    }

    fun personalityName(): String {
        return ""
    }

}