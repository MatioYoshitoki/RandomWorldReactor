package com.rw.random.domain.service

import com.rw.random.common.constants.BeingStatus
import com.rw.random.domain.entity.RWZone
import com.rw.random.domain.entity.obj.Fish
import com.rw.random.domain.repository.FishRepository
import com.rw.random.infra.handler.TaskHandler
import com.rw.random.infra.handler.WorldMessageDispatchHandler
import org.slf4j.LoggerFactory
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
    private val worldMessageDispatchHandler: WorldMessageDispatchHandler,
    private val taskHandler: TaskHandler,
) : PersistenceService {

    private val log = LoggerFactory.getLogger(this::class.java)
    override fun persistenceFish(fish: Fish): Mono<Void> {
        return fishRepository.saveOne(fish)
    }

    override fun loadFish(fishId: Long): Mono<Fish> {
        return fishRepository.findOne(fishId)
            .map {
                it.linkTheWorld(worldMessageDispatchHandler.worldChannel, taskHandler.taskHandler)
                it
            }
            .doOnNext {
                log.info("load fish: $it")
            }
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
