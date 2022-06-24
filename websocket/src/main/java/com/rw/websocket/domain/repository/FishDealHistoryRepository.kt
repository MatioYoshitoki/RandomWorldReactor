package com.rw.websocket.domain.repository

import com.rw.websocket.domain.entity.FishDealHistory
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Query.query
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface FishDealHistoryRepository {

    fun saveOne(fishDealHistory: FishDealHistory): Mono<FishDealHistory>

    fun findOne(orderId: Long): Mono<FishDealHistory>

    fun findAllBySellerId(userId: Long): Flux<FishDealHistory>

    fun findAllByBuyerId(userId: Long): Flux<FishDealHistory>

}

@Component
open class FishDealHistoryRepositoryImpl(
    private val entityTemplate: R2dbcEntityTemplate
) : FishDealHistoryRepository {
    override fun saveOne(fishDealHistory: FishDealHistory): Mono<FishDealHistory> {
        return entityTemplate.insert(fishDealHistory)
    }

    override fun findOne(orderId: Long): Mono<FishDealHistory> {
        return entityTemplate.selectOne(query(where("order_id").`is`(orderId)), FishDealHistory::class.java)
    }

    override fun findAllBySellerId(userId: Long): Flux<FishDealHistory> {
        return entityTemplate.select(query(where("seller_id").`is`(userId)), FishDealHistory::class.java)
    }

    override fun findAllByBuyerId(userId: Long): Flux<FishDealHistory> {
        return entityTemplate.select(query(where("buyer_id").`is`(userId)), FishDealHistory::class.java)
    }

}
