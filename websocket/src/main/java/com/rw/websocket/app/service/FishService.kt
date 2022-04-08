package com.rw.websocket.app.service

import com.rw.websocket.domain.repository.FishRepository
import com.rw.websocket.domain.repository.UserFishRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

interface FishService {

    fun fishExp(fishId: Long): Mono<Long>

    fun clearFish(fishId: Long): Mono<Long>

}

@Service
open class FishServiceImpl(
    private val userFishRepository: UserFishRepository,
    private val fishRepository: FishRepository,
) : FishService {
    override fun fishExp(fishId: Long): Mono<Long> {
        return fishRepository.findOne(fishId)
            .map { exchangeWeight2Exp(it.weight) }

    }

    override fun clearFish(fishId: Long): Mono<Long> {
        return fishRepository.deleteOne(fishId)
            .filter { it }
            .flatMap { userFishRepository.deleteOne(fishId) }
            .filter { it > 0 }
            .map { fishId }
    }

    private fun exchangeWeight2Exp(weight: Int): Long {
        return weight.toLong() / 1000
    }
}