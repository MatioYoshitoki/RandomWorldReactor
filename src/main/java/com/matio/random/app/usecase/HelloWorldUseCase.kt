package com.matio.random.app.usecase

import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

interface HelloWorldUseCase {

    fun runCase(): Mono<Void>

}

@Component
open class HelloWorldUseCaseImpl : HelloWorldUseCase {
    override fun runCase(): Mono<Void> {
        TODO("Not yet implemented")
    }

}