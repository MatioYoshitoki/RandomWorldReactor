package com.rw.websocket.domain.repository

import com.rw.websocket.domain.entity.FishSellLog
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import org.springframework.data.relational.core.query.Update.update
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface FishSellLogRepository {

    fun saveOne(fishSellLog: FishSellLog): Mono<FishSellLog>

    fun updateStatus(id: Long, status: Int): Mono<Boolean>

    fun findOne(orderId: Long): Mono<FishSellLog>

    fun findAllByUserId(userId: Long): Flux<FishSellLog>

    fun findAllByPage(orderBy: String, page: Int, pageSize: Int): Flux<FishSellLog>

}

@Component
open class FishSellLogRepositoryImpl(
    private val entityTemplate: R2dbcEntityTemplate
) : FishSellLogRepository {
    override fun saveOne(fishSellLog: FishSellLog): Mono<FishSellLog> {
        return entityTemplate.insert(fishSellLog)
    }

    override fun updateStatus(id: Long, status: Int): Mono<Boolean> {
        return entityTemplate.update(query(where("id").`is`(id)), update("status", status), FishSellLog::class.java)
            .map { it > 0 }
    }

    override fun findOne(orderId: Long): Mono<FishSellLog> {
        return entityTemplate.selectOne(query(where("id").`is`(orderId)), FishSellLog::class.java)
    }

    override fun findAllByUserId(userId: Long): Flux<FishSellLog> {
        return entityTemplate.select(query(where("seller_id").`is`(userId)), FishSellLog::class.java)
    }

    override fun findAllByPage(orderBy: String, page: Int, pageSize: Int): Flux<FishSellLog> {
        return entityTemplate.select(
            query(where("id").isNotNull).limit((page - 1) * pageSize).offset(pageSize.toLong()).sort(Sort.by(orderBy)),
            FishSellLog::class.java
        )
    }

}
