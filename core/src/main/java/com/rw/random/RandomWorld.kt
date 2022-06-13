package com.rw.random

import com.rw.random.app.usecase.HelloWorldUseCase
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.reactive.config.EnableWebFlux

@EnableScheduling
@SpringBootApplication(
    exclude = [WebMvcAutoConfiguration::class]
)
@EnableWebFlux
@EnableAsync
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
