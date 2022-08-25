package com.rw.websocket.app.service

import com.rw.random.common.constants.BeingStatus
import com.rw.websocket.domain.dto.request.FishDetails
import com.rw.websocket.domain.entity.FishDealHistory
import com.rw.websocket.domain.entity.FishSellLog
import com.rw.websocket.domain.repository.*
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty

interface FishService {

    fun getFishDetail(fishId: Long): Mono<FishDetails>

    fun cleanFish(fishId: Long): Mono<Long>

    fun checkFishOwner(fishId: Long, userName: String): Mono<Boolean>

    fun getSellingFishByOrderId(orderId: Long): Mono<FishSellLog>

    fun getSellingFish(orderBy: String, page: Int, pageSize: Int): Flux<FishSellLog>

    fun sellFish(fishDetails: FishDetails, userName: String, userId: Long, price: Long): Mono<Void>

    fun changeFishStatus(fishId: Long, status: BeingStatus): Mono<Void>

    fun soldOutFish(userName: String, orderId: Long, fishId: Long): Mono<Void>

    fun finishFishOrder(userId: Long, userName: String, fishSellLog: FishSellLog): Mono<Boolean>

}

@Service
open class FishServiceImpl(
    private val userFishRepository: UserFishRepository,
    private val fishRepository: FishRepository,
    private val userRepository: UserRepository,
    private val fishSellLogRepository: FishSellLogRepository,
    private val fishSoldOutLogRepository: FishSoldOutLogRepository,
    private val fishDealHistoryRepository: FishDealHistoryRepository
) : FishService {
    override fun getFishDetail(fishId: Long): Mono<FishDetails> {
        return fishRepository.findOne(fishId)
    }

    override fun cleanFish(fishId: Long): Mono<Long> {
        return fishRepository.deleteOne(fishId)
            .filter { it }
            .flatMap { userFishRepository.deleteOne(fishId) }
            .filter { it > 0 }
            .map { fishId }
    }

    override fun checkFishOwner(fishId: Long, userName: String): Mono<Boolean> {
        return userFishRepository.findFishOwner(fishId)
            .flatMap {
                userRepository.findOneByUserName(userName)
                    .map { user ->
                        user.id == it
                    }
            }
            .defaultIfEmpty(false)
    }

    override fun getSellingFishByOrderId(orderId: Long): Mono<FishSellLog> {
        return fishSellLogRepository.findOne(orderId)
    }

    override fun getSellingFish(orderBy: String, page: Int, pageSize: Int): Flux<FishSellLog> {
        return fishSellLogRepository.findAllByPage(orderBy, page, pageSize)
    }

    override fun sellFish(fishDetails: FishDetails, userName: String, userId: Long, price: Long): Mono<Void> {
        return fishSellLogRepository.saveOne(
            FishSellLog.of(
                userId.toString(),
                userName,
                fishDetails.id,
                fishDetails.name,
                fishDetails,
                price.toInt()
            )
        )
            .flatMap { fishSellLog ->
                changeFishStatus(fishSellLog.fishId.toLong(), BeingStatus.SELLING)
            }
    }

    override fun changeFishStatus(fishId: Long, status: BeingStatus): Mono<Void> {
        return userFishRepository.updateFishStatus(fishId, status)
            .flatMap {
                if (it > 0) {
                    fishRepository.updateFishStatus(fishId, status)
                } else {
                    Mono.empty()
                }
            }
    }

    override fun soldOutFish(userName: String, orderId: Long, fishId: Long): Mono<Void> {
        return fishSellLogRepository.updateStatus(orderId, 1)
            .filter { it }
            .flatMap {
                userFishRepository.updateFishStatus(fishId, BeingStatus.SLEEP)
                    .flatMap {
                        if (it > 0) {
                            fishRepository.updateFishStatus(fishId, BeingStatus.SLEEP)
                        } else {
                            Mono.empty()
                        }
                    }
            }
    }

    override fun finishFishOrder(userId: Long, userName: String, fishSellLog: FishSellLog): Mono<Boolean> {
        return fishSellLogRepository.updateStatus(fishSellLog.id!!.toLong(), 2)
            .flatMap {
                if (it) {
                    fishDealHistoryRepository.saveOne(FishDealHistory.of(fishSellLog, userId, userName))
                        .map { true }
                        .switchIfEmpty(
                            fishSellLogRepository.updateStatus(fishSellLog.id!!.toLong(), 3)
                                .map { false }
                        )
                } else {
                    Mono.just(false)
                }
            }
    }

}
