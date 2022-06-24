package com.rw.websocket.app.usecase

import com.rw.websocket.app.service.FishService
import com.rw.websocket.domain.entity.FishSellLog
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

interface ViewFishMarketUseCase {

    fun runCase(orderId: Long?, orderBy: String?, page: Int?, pageSize: Int?): Flux<FishSellLog>

}

@Component
open class ViewFishMarketUseCaseImpl(
    private val fishService: FishService,
) : ViewFishMarketUseCase {
    override fun runCase(orderId: Long?, orderBy: String?, page: Int?, pageSize: Int?): Flux<FishSellLog> {
        if (orderId != null) {
            return Flux.from(fishService.getSellingFishByOrderId(orderId))
        }
        return fishService.getSellingFish(orderBy ?: "create_time", page ?: 0, pageSize ?: 20)
    }

}
