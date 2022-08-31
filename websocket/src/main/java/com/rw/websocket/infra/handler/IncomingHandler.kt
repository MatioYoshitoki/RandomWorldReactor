package com.rw.websocket.infra.handler

import com.fasterxml.jackson.core.async_.JsonFactory
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import com.rw.websocket.app.service.UserService
import com.rw.websocket.infra.contants.ActionTypes
import com.rw.websocket.infra.contants.HttpConstants
import com.rw.websocket.infra.session.DefaultRandomWorldSessionManager
import com.rw.websocket.infra.session.SessionMetadataUtils
import com.rw.websocket.infra.subscription.SubscriptionRegistry
import com.rw.websocket.infra.utils.SimpleMessageUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.integration.channel.FluxMessageChannel
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import org.springframework.util.Assert
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactivejson.ReactorObjectReader
import reactor.core.publisher.Mono
import reactor.util.function.Tuples
import java.util.*

@Component
open class IncomingHandler(
    @Qualifier("inboundChannel")
    private val clientInboundChannelFlux: FluxMessageChannel,
    private val objectMapper: ObjectMapper,
    private val brokerSessionManager: DefaultRandomWorldSessionManager,
    private val subscriptionRegistry: SubscriptionRegistry,
    private val userService: UserService
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val jsonReader: ReactorObjectReader = ReactorObjectReader(JsonFactory())
    private var mapReader: ObjectReader? = null
    private val mapTypeRef: TypeReference<HashMap<String, Any>> = object : TypeReference<HashMap<String, Any>>() {}

    init {
        this.mapReader = getObjectReader(mapTypeRef)
    }

    private fun getObjectReader(typeRef: TypeReference<*>): ObjectReader? {
        return Objects.requireNonNull(objectMapper).readerFor(typeRef)
    }


    @Suppress("UNCHECKED_CAST")
    fun handleMessageFromClient(session: WebSocketSession, message: WebSocketMessage): Mono<Void> {
        if (message.type != WebSocketMessage.Type.TEXT) {
            return Mono.error(IllegalArgumentException("暂时只支持 JSON 文本格式"))
        }
        val msgText = message.payloadAsText

        var msgMap: Map<*, *> = HashMap<Any, Any>()
        try {
            msgMap = objectMapper.readValue(msgText, Map::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Mono.just(msgMap)
            .map {
                it as Map<String, *>
            }
            .map { Tuples.of(msgText, buildHeaders(session, it)) }
            .doOnNext {
                log.debug("Receive message from client: {}, {}", it.t1, it.t2)
            }
            .map { MessageBuilder.withPayload(it.t1).copyHeaders(it.t2).build() }
            .delayUntil {
                // 订阅默认渠道，并初始化 metadata
                if (SimpleMessageUtils.getActionType(it) == ActionTypes.LOGIN) {
                    initChannelsAndMetadata(it, session)
                } else {
                    Mono.empty<Void>()
                }
            }
            .map {
                session.textMessage(it.payload)
            }
            .flatMap { session.send(Mono.just(it)) }
            .onErrorResume {
                log.error("处理客户端消息失败", it)
                Mono.empty()
            }
            .then()
    }

    private fun initChannelsAndMetadata(message: Message<*>, session: WebSocketSession): Mono<Void> {
        return session.handshakeInfo.principal
            .flatMap {
                userService.getUserByUserName(it.name)
            }
            .doOnNext {
                log.debug("user login: ${it.id}")
                val metadata: MutableMap<String, Any> = hashMapOf()
                val ip = SimpleMessageUtils.getRemoteIp(message)
                metadata[DefaultRandomWorldSessionManager.IP_KEY] = ip ?: ""
                val uid = it.id
                metadata[DefaultRandomWorldSessionManager.UID_KEY] = uid
                log.info("Subscribe message={}, metadata={}", message, metadata)
//        subscriptionsRegistry.initSubscribeChannels(message)
                // 订阅 session 频道
//                subscriptionRegistry.subscribe(session.id, SimpleMessageUtils.buildSessionDestination(session.id))
                // 订阅 user 频道
                subscriptionRegistry.subscribe(session.id, SimpleMessageUtils.buildUserDestination(uid))
                // 订阅 world 频道
                subscriptionRegistry.subscribe(session.id, SimpleMessageUtils.buildWorldDestination())
                // 初始化元数据
                brokerSessionManager.setMetadata(session.id, metadata)
            }
            .then()

    }

    @Suppress("DuplicatedCode")
    private fun buildHeaders(session: WebSocketSession, map: Map<String, *>): MutableMap<String, Any> {
        Assert.notNull(session, "Session must not be null")
        Assert.notNull(map, "Json object must not be null")
        val httpHeaders = session.handshakeInfo.headers
        val actionType = map["type"].toString()
        val destination = SimpleMessageUtils.buildAppDestination(actionType)
        var userId: Long? = null
        if (map.containsKey(SimpleMessageUtils.FIELD_USER_ID)) {
            userId = map[SimpleMessageUtils.FIELD_USER_ID].toString().toLong()
        }
        val metadata = brokerSessionManager.getMetadata(session.id)
        // 最后通过 sessionId 反查 userId
        if (userId == null) {
            userId = SessionMetadataUtils.getUid(metadata)
        }
        val address = session.handshakeInfo.remoteAddress
        var remoteIp: String? = null
        if (address != null) {
            remoteIp = address.address.hostAddress
        }

        if (remoteIp.isNullOrBlank()) {
            remoteIp = httpHeaders[HttpConstants.X_FORWARDED_FOR_HEADER]?.first()
        }
        if (remoteIp.isNullOrBlank()) {
            return mutableMapOf()
        }
        val headers = hashMapOf<String, Any>()
        if (userId != null) {
            headers[SimpleMessageUtils.USER_ID_HEADER] = userId
        }

        headers[SimpleMessageUtils.DESTINATION_HEADER] = destination
        headers[SimpleMessageUtils.SESSION_ID_HEADER] = session.id
        headers[SimpleMessageUtils.ACTION_TYPE_HEADER] = actionType
        headers[SimpleMessageUtils.REMOTE_IP_HEADER] = remoteIp
        log.info(
            "Receive Message from client sessionId={}, type={}, uid={}, roomId={}",
            session.id,
            actionType,
            userId,
        )
        return headers
    }

}

