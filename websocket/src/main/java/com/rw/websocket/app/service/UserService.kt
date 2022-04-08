package com.rw.websocket.app.service

import com.rw.websocket.domain.repository.UserPropertyRepository
import com.rw.websocket.domain.repository.UserRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface UserService {

    fun eatFish(fishId: Long, userId: Long, accessToken: String): Mono<Boolean>

}

@Component
open class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userPropertyRepository: UserPropertyRepository,
    private val fishService: FishService
) : UserService {
    override fun eatFish(fishId: Long, userId: Long, accessToken: String): Mono<Boolean> {
        return userPropertyRepository.findOne(userId)
            .flatMap { property ->
                fishService.fishExp(fishId)
                    .filterWhen {
                        userPropertyRepository.updateExp(userId, property.exp ?: (0 + it))
                    }
                    .map {
                        property.exp ?: (0 + it)
                    }
            }
            .flatMap {
                userRepository.updateUserWithProperty(accessToken, mapOf("exp" to it.toString()))
            }
    }
}