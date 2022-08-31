package com.rw.websocket.infra.config

import io.netty.handler.timeout.IdleStateHandler
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.stereotype.Component
import reactor.netty.http.server.HttpServer

@Component
@EnableConfigurationProperties(ApplicationProperties::class)
open class NettyReactiveWebServerFactoryCustomizer(
    private val properties: ApplicationProperties
) : WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun customize(serverFactory: NettyReactiveWebServerFactory) {
        log.info("Custom Netty Server")
        serverFactory
            .addServerCustomizers(WebSocketNettyCustomizer(properties.netty))
    }
}

class WebSocketNettyCustomizer(private val nettyProperties: ApplicationProperties.NettyProperties) :
    NettyServerCustomizer {
    override fun apply(server: HttpServer): HttpServer {
        return server
            .doOnConnection { conn ->
                if (nettyProperties.idleTimeout.enabled) {
                    conn.addHandler(
                        IdleStateHandler(
                            nettyProperties.idleTimeout.writeTimeoutSeconds,
                            nettyProperties.idleTimeout.readTimeoutSeconds,
                            nettyProperties.idleTimeout.allTimeoutSeconds,
                        )
                    )
                }
            }
    }
}