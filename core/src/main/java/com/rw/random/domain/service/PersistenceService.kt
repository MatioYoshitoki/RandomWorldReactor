package com.rw.random.domain.service

import com.rw.random.common.constants.BeingStatus
import com.rw.random.domain.entity.RWZone
import com.rw.random.domain.entity.obj.Fish
import com.rw.random.domain.repository.FishRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.SimpleTimeZone

interface PersistenceService {

    fun persistenceFish(fish: Fish): Mono<Void>

    fun loadFish(fishId: Long): Mono<Fish>

    fun loadAllAliveFish(): Mono<Void>

    fun cleanFish(): Mono<Void>

}

@Component
open class PersistenceServiceImpl(
    private val fishRepository: FishRepository,
    private val zone: RWZone,
) : PersistenceService {
    override fun persistenceFish(fish: Fish): Mono<Void> {
        return fishRepository.saveOne(fish)
    }

    override fun loadFish(fishId: Long): Mono<Fish> {
        return fishRepository.findOne(fishId)
    }

    override fun loadAllAliveFish(): Mono<Void> {
        return fishRepository.findAll()
            .filter { it.status == BeingStatus.ALIVE }
            .doOnNext { zone.enterZone(it) }
            .then()
    }

    override fun cleanFish(): Mono<Void> {
        return fishRepository.findAll()
            .filter {
                it.status == BeingStatus.DEAD && !it.hasMaster
            }
            .flatMap {
                fishRepository.deleteOne(it.id)
            }
            .then()
    }

}
