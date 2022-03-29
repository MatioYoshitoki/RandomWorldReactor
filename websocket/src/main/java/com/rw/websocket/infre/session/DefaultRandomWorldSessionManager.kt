package com.rw.websocket.infre.session

import com.rw.websocket.infre.config.ApplicationProperties
import org.springframework.messaging.Message
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Sinks
import reactor.util.concurrent.Queues
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

data class BrokerSession(
    val channelBufferSize: Int,
    val webSocketSession: WebSocketSession,
    val metadata: MutableMap<String, Any> = ConcurrentHashMap(),
    val channel: Sinks.Many<Message<*>> =
        Sinks.many().unicast()
            .onBackpressureBuffer(Queues.get<Message<*>>(channelBufferSize).get()),
//    var disposable: Disposable? = null
)

@Component
open class DefaultRandomWorldSessionManager(
    private val applicationProperties: ApplicationProperties
) {

    private val sessionStore: MutableMap<String, BrokerSession> = ConcurrentHashMap<String, BrokerSession>()

    private val lock = ReentrantLock()

    fun getOrCreate(wsSession: WebSocketSession): BrokerSession {
        lock.withLock {
            return sessionStore.computeIfAbsent(wsSession.id) {
                BrokerSession(128, wsSession)
            }
        }
    }

    fun setMetadata(sessionId: String, metaKey: String, metaValue: Any) {
        this.sessionStore[sessionId]?.metadata?.put(metaKey, metaValue)
    }

    fun setMetadata(sessionId: String, metadata: Map<String, Any>) {
        this.sessionStore[sessionId]?.metadata?.putAll(metadata)
    }

    fun getMetadata(sessionId: String): Map<String, Any> {
        return this.sessionStore[sessionId]?.metadata ?: mapOf()
    }

    fun remove(sessionId: String) {
//        val session =
        sessionStore.remove(sessionId)
//        session?.channel?.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST)
//        if (session?.disposable != null && session.disposable?.isDisposed != true) {
//            session.disposable?.dispose()
//        }
    }

    fun find(sessionId: String): BrokerSession? {
        return sessionStore[sessionId]
    }

    fun findAll(): List<BrokerSession> {
        return sessionStore.values.toList()
    }

    companion object {
        const val VERSION_KEY = "version"
        const val UID_KEY = "uid"
        const val ROOM_ID_KEY = "room_id"
        const val SERVER_KEY = "server_id"
        const val IS_COMPRESS_KEY = "is_compress"
        const val IP_KEY = "ip"
    }
}

object SessionMetadataUtils {

    fun getUid(metadata: Map<String, Any>): Long? {
        return metadata[DefaultRandomWorldSessionManager.UID_KEY]?.toString()?.toLongOrNull()
    }

}