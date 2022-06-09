package com.rw.websocket.app.service

import com.rw.websocket.domain.dto.request.FishDetails
import com.rw.websocket.domain.repository.FishRepository
import com.rw.websocket.domain.repository.UserFishRepository
import com.rw.websocket.domain.repository.redis.AccessTokenUserRepository
import com.rw.websocket.domain.repository.redis.UserInfoRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

interface FishService {

    fun getFishDetail(fishId: Long): Mono<FishDetails>

    fun clearFish(fishId: Long): Mono<Long>

    fun checkFishOwner(fishId: Long, userId: Long): Mono<Boolean>

}

@Service
open class FishServiceImpl(
    private val userFishRepository: UserFishRepository,
    private val fishRepository: FishRepository,
    private val accessTokenUserRepository: AccessTokenUserRepository,
    private val userInfoRepository: UserInfoRepository
) : FishService {
    override fun getFishDetail(fishId: Long): Mono<FishDetails> {
        return fishRepository.findOne(fishId)
    }

    override fun clearFish(fishId: Long): Mono<Long> {
        return fishRepository.deleteOne(fishId)
            .filter { it }
            .flatMap { userFishRepository.deleteOne(fishId) }
            .filter { it > 0 }
            .map { fishId }
    }

    override fun checkFishOwner(fishId: Long, userId: Long): Mono<Boolean> {
        return userInfoRepository.findOneUserProperty(userId)
            .flatMap { user ->
                userFishRepository.findFishOwner(fishId)
                    .map {
                        it == user.userId
                    }
            }
            .defaultIfEmpty(false)
    }

}
