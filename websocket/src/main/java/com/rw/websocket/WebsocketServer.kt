package com.rw.websocket

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.reactive.config.EnableWebFlux

@EnableWebFlux
@EnableScheduling
@SpringBootApplication
open class WebsocketServer

fun main(args: Array<String>) {
    runApplication<WebsocketServer>(*args)
}
