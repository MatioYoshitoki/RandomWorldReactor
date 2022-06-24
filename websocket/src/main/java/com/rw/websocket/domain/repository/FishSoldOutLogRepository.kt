package com.rw.websocket.domain.repository

import com.rw.websocket.domain.entity.FishSoldOutLog
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface FishSoldOutLogRepository {

    fun saveOne(fishSellLog: FishSoldOutLog): Mono<Void>

    fun findOne(orderId: Long): Mono<FishSoldOutLog>


}

@Component
open class FishSoleOutLogRepositoryImpl(
    private val entityTemplate: R2dbcEntityTemplate
) : FishSoldOutLogRepository {

    override fun saveOne(fishSellLog: FishSoldOutLog): Mono<Void> {
        return entityTemplate.insert(fishSellLog)
            .then()
    }

    override fun findOne(orderId: Long): Mono<FishSoldOutLog> {
        return entityTemplate.selectOne(query(where("order_id").`is`(orderId)), FishSoldOutLog::class.java)
    }

}
