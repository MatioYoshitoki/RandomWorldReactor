package com.rw.random.domain.repository

import com.rw.random.common.constants.BeingStatus
import com.rw.random.domain.entity.RWPersonality
import com.rw.random.domain.entity.obj.Fish
import com.rw.random.infra.config.TaskProperties
import com.rw.random.infra.handler.TaskHandler
import com.rw.random.infra.handler.WorldMessageDispatchHandler
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface FishRepository {

    fun findOne(fishId: Long): Mono<Fish>

    fun saveOne(fish: Fish): Mono<Void>

}

@Component
open class FishRepositoryImpl(
    private val redisTemplate: ReactiveStringRedisTemplate,
    private val taskProperties: TaskProperties,
    private val worldMessageDispatchHandler: WorldMessageDispatchHandler,
    private val taskHandler: TaskHandler
) : FishRepository {
    override fun findOne(fishId: Long): Mono<Fish> {
        return redisTemplate.opsForHash<String, String>()
            .entries(getKey(fishId))
            .collectList()
            .map { list ->
                list.associate { Pair(it.key, it.value) }
            }
            .filter {
                for (key in Fish.KEYS) {
                    if (!it.containsKey(key)) {
                        return@filter false
                    }
                }
                true
            }
            .map {
                Fish(
                    it["id"]!!.toLong(),
                    it["name"]!!.toString(),
                    it["hasMaster"]!!.toBoolean(),
                    it["weight"]!!.toLong(),
                    it["maxHeal"]!!.toInt(),
                    it["heal"]!!.toInt(),
                    it["recoverSpeed"]!!.toInt(),
                    it["atk"]!!.toInt(),
                    it["def"]!!.toInt(),
                    it["earnSpeed"]!!.toInt(),
                    it["dodge"]!!.toInt(),
                    it["money"]!!.toLong(),
                    taskProperties,
                    worldMessageDispatchHandler.worldChannel,
                    taskHandler.taskHandler,
                    BeingStatus.valueOf(it["status"]!!),
                    RWPersonality(it["personalityId"]!!.toInt(), it["personalityRandomRate"]!!.toInt())
                )
            }
    }

    override fun saveOne(fish: Fish): Mono<Void> {
        return Mono.just(fish)
            .map {
                mapOf(
                    "id" to it.id.toString(),
                    "name" to it.name,
                    "hasMaster" to it.hasMaster.toString(),
                    "weight" to it.weight.toString(),
                    "maxHeal" to it.maxHeal.toString(),
                    "heal" to it.heal.toString(),
                    "recoverSpeed" to it.recoverSpeed.toString(),
                    "atk" to it.atk.toString(),
                    "def" to it.def.toString(),
                    "earnSpeed" to it.earnSpeed.toString(),
                    "dodge" to it.dodge.toString(),
                    "money" to it.money.toString(),
                    "status" to it.status.name,
                    "personalityId" to it.personality.personality.toString(),
                    "personalityRandomRate" to it.personality.originRandomRate.toString()
                )
            }
            .flatMap { map ->
                redisTemplate.opsForHash<String, String>()
                    .putAll(getKey(fish.id), map.toMutableMap())
            }
            .then()

    }

    private fun getKey(fishId: Long): String {
        return "fish:$fishId"
    }
}
