package com.rw.websocket.infre.config

import com.rw.websocket.infre.handler.DefaultMessageBrokerRelayPublisher
import com.rw.websocket.infre.handler.RandomWorldWebSocketHandler
import org.springframework.beans.factory.annotation.Configurable
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.integration.channel.FluxMessageChannel
import org.springframework.integration.endpoint.ReactiveStreamsConsumer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler

@Configurable
@Component
@EnableConfigurationProperties(ApplicationProperties::class)
open class ApplicationConfiguration(
    private val applicationProperties: ApplicationProperties
) {

    @Bean("inboundChannel")
    open fun clientInboundChannelFlux(): FluxMessageChannel {
        return FluxMessageChannel()
    }

    @Bean("outboundChannel")
    open fun clientOutboundChannelFlux(): FluxMessageChannel {
        return FluxMessageChannel()
    }

    @Bean
    open fun clientInboundChannelConsumer(
        @Qualifier("inboundChannel")
        clientInboundChannel: FluxMessageChannel,
        redisTemplate: ReactiveStringRedisTemplate
    ): ReactiveStreamsConsumer {
        return ReactiveStreamsConsumer(clientInboundChannel, DefaultMessageBrokerRelayPublisher(redisTemplate))
    }

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