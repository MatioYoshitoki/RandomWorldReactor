package com.rw.random.domain.service

import com.rw.random.domain.entity.obj.Fish
import com.rw.random.domain.repository.FishRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface PersistenceService {

    fun persistenceFish(fish: Fish): Mono<Void>

    fun loadFish(fishId: Long): Mono<Fish>

}

@Component
open class PersistenceServiceImpl(
    private val fishRepository: FishRepository
) : PersistenceService {
    override fun persistenceFish(fish: Fish): Mono<Void> {
        return fishRepository.saveOne(fish)
    }

    override fun loadFish(fishId: Long): Mono<Fish> {
        return fishRepository.findOne(fishId)
    }

}