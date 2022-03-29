package com.rw.random

import com.rw.random.app.usecase.HelloWorldUseCase
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
open class RandomWorld(
    private val helloWorldUseCase: HelloWorldUseCase
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        helloWorldUseCase.runCase()
    }

}

fun main(args: Array<String>) {
    runApplication<RandomWorld>(*args)
}
