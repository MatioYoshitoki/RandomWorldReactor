package com.rw.random.domain.entity

import java.util.*
import kotlin.reflect.KClass

class CrazyPersonality : RWPersonality(999999, 0) {
    init {
        val crazyPersonal = sortedMapOf(
            9500 to ATKTask::class,
            10000 to StayTask::class,
        )
        val personalityMap: MutableMap<KClass<out RWEvent>, SortedMap<Int, KClass<out RWTask>>> = mutableMapOf(
            ATKEvent::class to crazyPersonal,
            EarnEvent::class to crazyPersonal,
            TimeEvent::class to crazyPersonal,
        )
        this.eventBehavior = personalityMap
    }

}
