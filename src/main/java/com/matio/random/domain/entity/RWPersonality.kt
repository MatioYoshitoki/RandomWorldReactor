package com.matio.random.domain.entity

import cn.hutool.core.util.RandomUtil
import com.matio.random.domain.entity.obj.Being
import java.util.*
import kotlin.reflect.KClass

open class RWPersonality(
    private val eventBehavior: Map<KClass<out RWEvent>, SortedMap<Int, KClass<out RWTask>>>
) {

    init {
        this.eventBehavior.entries.forEach {
            val tmp = it.value
            if (tmp.keys.last() != 10000) {
                throw IllegalArgumentException()
            }
        }
    }

    fun randomTask(event: RWEvent, obj: Being): RWTask? {
        val taskBehavior = eventBehavior[event::class]!!
        val rate: Int = RandomUtil.randomInt(10000)
        var klazz: KClass<out RWTask>? = null
        for (entry in taskBehavior) {
            if (rate <= entry.key) {
                klazz = entry.value
                break
            }
        }
        return when (klazz) {
            ATKTask::class -> {
                if (event is ATKEvent) {
                    ATKTask(obj, event.source!! as Being)
                } else {
                    val target = obj.findAllHumanSameZone().filter { it != obj }.findAny()
                    if (target.isPresent) {
                        ATKTask(obj, target.get())
                    } else {
                        null
                    }
                }
            }
            EarnTask::class -> EarnTask(obj, obj.tryEarn())
            else -> StayTask(obj)
        }
    }

    fun personalityName(): String {
        return ""
    }

}