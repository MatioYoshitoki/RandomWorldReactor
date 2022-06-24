package com.rw.websocket.domain.repository

import com.rw.websocket.domain.entity.FishSellLog
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface FishSellLogRepository {

    fun saveOne(fishSellLog: FishSellLog): Mono<Void>

    fun findOne(orderId: Long): Mono<FishSellLog>

    fun findAllByUserId(userId: Long): Flux<FishSellLog>

}
