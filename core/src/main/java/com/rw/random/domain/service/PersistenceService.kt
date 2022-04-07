package com.rw.random.domain.service

import com.rw.random.domain.entity.obj.Fish
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface PersistenceService {

    fun persistenceFish(fish: Fish): Mono<Fish>

}

@Component
open class PersistenceServiceImpl: PersistenceService {
    override fun persistenceFish(fish: Fish): Mono<Fish> {
        TODO("Not yet implemented")
    }

}