package com.rw.websocket.domain.repository

import com.rw.random.common.constants.BeingStatus
import com.rw.websocket.domain.dto.request.FishDetails
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface FishRepository {

    fun findOne(fishId: Long): Mono<FishDetails>

    fun updateMasterId(fishId: Long, masterId: Long): Mono<Void>

    fun deleteOne(fishId: Long): Mono<Boolean>
}

@Component
open class FishRepositoryImpl(
    private val redisTemplate: ReactiveStringRedisTemplate,
) : FishRepository {
    override fun findOne(fishId: Long): Mono<FishDetails> {
        return redisTemplate.opsForHash<String, String>()
            .entries(getKey(fishId))
            .collectList()
            .map { list ->
                list.associate { Pair(it.key, it.value) }
            }
            .filter {
                for (key in FishDetails.KEYS) {
                    if (!it.containsKey(key)) {
                        return@filter false
                    }
                }
                true
            }
            .map {
                FishDetails(
                    it["id"]!!.toLong(),
                    it["name"]!!.toString(),
                    it["hasMaster"]!!.toBoolean(),
                    it["weight"]!!.toInt(),
                    it["maxHeal"]!!.toInt(),
                    it["heal"]!!.toInt(),
                    it["recoverSpeed"]!!.toInt(),
                    it["atk"]!!.toInt(),
                    it["def"]!!.toInt(),
                    it["earnSpeed"]!!.toInt(),
                    it["dodge"]!!.toInt(),
                    it["money"]!!.toInt(),
                    BeingStatus.valueOf(it["status"]!!),
                    it["personalityId"]!!.toInt()
                )
            }
    }

    override fun updateMasterId(fishId: Long, masterId: Long): Mono<Void> {
        return redisTemplate.opsForHash<String, String>()
            .putIfAbsent(getKey(fishId), "masterId", masterId.toString())
            .then()
    }

    override fun deleteOne(fishId: Long): Mono<Boolean> {
        return redisTemplate.opsForHash<String, String>()
            .delete(getKey(fishId))
    }

    private fun getKey(fishId: Long): String {
        return "fish:$fishId"
    }
}
