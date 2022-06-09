package com.rw.websocket

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.reactive.config.EnableWebFlux

@EnableWebFlux
@EnableScheduling
@SpringBootApplication(
    scanBasePackages = ["com.rw.websocket", "com.rw.random"],
    exclude = [WebMvcAutoConfiguration::class]
)
open class WebsocketServer

fun main(args: Array<String>) {
    runApplication<WebsocketServer>(*args)
}
