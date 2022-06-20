package com.rw.websocket.app.service

import com.rw.websocket.domain.dto.request.FishDetails
import com.rw.websocket.domain.repository.FishRepository
import com.rw.websocket.domain.repository.UserFishRepository
import com.rw.websocket.domain.repository.UserRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

interface FishService {

    fun getFishDetail(fishId: Long): Mono<FishDetails>

    fun cleanFish(fishId: Long): Mono<Long>

    fun checkFishOwner(fishId: Long, userName: String): Mono<Boolean>

}

@Service
open class FishServiceImpl(
    private val userFishRepository: UserFishRepository,
    private val fishRepository: FishRepository,
    private val userRepository: UserRepository,
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
//        userInfoRepository.findOneUserProperty(userId)
//            .flatMap { user ->
//                userFishRepository.findFishOwner(fishId)
//                    .map {
//                        it == user.userId
//                    }
//            }
//            .defaultIfEmpty(false)
    }

}
