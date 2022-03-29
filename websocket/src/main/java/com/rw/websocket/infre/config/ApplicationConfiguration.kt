package com.rw.websocket.infre.config

import com.rw.websocket.infre.handler.RandomWorldWebSocketHandler
import org.springframework.beans.factory.annotation.Configurable
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler

@Configurable
open class ApplicationConfiguration {
    @Bean
    open fun handlerMapping(
        handler: RandomWorldWebSocketHandler
    ): HandlerMapping {
        val map: HashMap<String, WebSocketHandler> = HashMap()
        map["/"] = handler
        val order = -1
        return SimpleUrlHandlerMapping(map, order)
    }
}