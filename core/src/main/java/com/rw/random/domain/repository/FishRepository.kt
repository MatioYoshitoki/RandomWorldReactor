package com.rw.random.domain.repository

import com.rw.random.common.constants.BeingStatus
import com.rw.random.domain.entity.RWEvent
import com.rw.random.domain.entity.RWPersonality
import com.rw.random.domain.entity.RWTask
import com.rw.random.domain.entity.obj.Fish
import com.rw.random.infra.config.ApplicationProperties
import com.rw.random.infra.config.TaskProperties
import com.rw.random.infra.handler.TaskHandler
import com.rw.random.infra.handler.WorldMessageDispatchHandler
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks

interface FishRepository {

    fun findOne(fishId: Long): Mono<Fish>

    fun findAll(): Flux<Fish>

    fun updateFishStatus(fishId: Long, status: BeingStatus): Mono<Void>

    fun saveOne(fish: Fish): Mono<Void>

    fun deleteOne(fishId: Long): Mono<Void>

}

@Component
open class FishRepositoryImpl(
    private val applicationProperties: ApplicationProperties,
    private val redisTemplate: ReactiveStringRedisTemplate,
    private val taskProperties: TaskProperties
) : FishRepository {

    private val log = LoggerFactory.getLogger(javaClass)

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
            .map { convertToFish(it) }
    }

    override fun findAll(): Flux<Fish> {
        val option = ScanOptions.ScanOptionsBuilder().match("fish:*").build()
        return redisTemplate.scan(option)
            .flatMap { key ->
                redisTemplate.opsForHash<String, String>()
                    .entries(key)
                    .collectList()
                    .map { list ->
                        list.associate { Pair(it.key, it.value) }
                    }
                    .filter {
                        for (k in Fish.KEYS) {
                            if (!it.containsKey(k)) {
                                return@filter false
                            }
                        }
                        true
                    }
                    .map { convertToFish(it) }
            }
    }

    override fun updateFishStatus(fishId: Long, status: BeingStatus): Mono<Void> {
        return redisTemplate.opsForHash<String, String>()
            .put(getKey(fishId), "status", status.name)
            .then()
    }

    override fun saveOne(fish: Fish): Mono<Void> {
        return Mono.just(fish)
            .map {
                val map = mutableMapOf(
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
                if (it.masterId?.toString() != null) {
                    map["masterId"] = it.masterId.toString()
                }
                map
            }
            .flatMap { map ->
                redisTemplate.opsForHash<String, String>()
                    .putAll(getKey(fish.id), map)
            }
            .then()

    }

    override fun deleteOne(fishId: Long): Mono<Void> {
        return redisTemplate.delete(getKey(fishId))
            .then()
    }

    private fun convertToFish(map: Map<String, String>): Fish {
        val personalityIdx = map["personalityId"]!!.toInt()
        return Fish(
            map["id"]!!.toLong(),
            map["name"]!!.toString(),
            map["hasMaster"]!!.toBoolean(),
            map["masterId"]?.toLong(),
            map["weight"]!!.toLong(),
            map["maxHeal"]!!.toInt(),
            map["heal"]!!.toInt(),
            map["recoverSpeed"]!!.toInt(),
            map["atk"]!!.toInt(),
            map["def"]!!.toInt(),
            map["earnSpeed"]!!.toLong(),
            map["dodge"]!!.toInt(),
            map["money"]!!.toLong(),
            taskProperties,
            status = BeingStatus.valueOf(map["status"]!!),
            personality = RWPersonality(personalityIdx, applicationProperties.personalityName[personalityIdx], map["personalityRandomRate"]!!.toInt())
        )
    }

    private fun getKey(fishId: Long): String {
        return "fish:$fishId"
    }
}
