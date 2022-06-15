package com.rw.random.infra.utils

import org.slf4j.LoggerFactory
import reactor.core.publisher.SignalType
import reactor.core.publisher.Sinks
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.LockSupport

object SinksUtils {

    private val log = LoggerFactory.getLogger(javaClass)

    @Throws
    fun <T> tryEmit(sink: Sinks.Many<in T>, item: T, timeout: Int = 10) {
        synchronized(this) {
//            var remainingTime = 0
//            if (timeout > 0) {
//                remainingTime = timeout
//            }
//            val parkTimeout = 10L
//            val parkTimeoutNs = TimeUnit.MILLISECONDS.toNanos(parkTimeout)
            if (!tryEmit(sink, item)) {
                throw IllegalStateException("The [$sink] overflow or non serialized")
            }
//            while (!tryEmit(sink, item)) {
//                remainingTime -= parkTimeout.toInt()
//                if (timeout >= 0 && remainingTime <= 0) {
//                    throw IllegalStateException("The [$sink] overflow or non serialized")
//                }
//                log.debug("Sink emit failed and retry")
//                LockSupport.parkNanos(parkTimeoutNs)
//            }
        }
    }

    @Throws
    private fun <T> tryEmit(sink: Sinks.Many<in T>, item: T): Boolean {
        return when (val res = sink.tryEmitNext(item)) {
            Sinks.EmitResult.OK -> true
            Sinks.EmitResult.FAIL_OVERFLOW, Sinks.EmitResult.FAIL_NON_SERIALIZED -> {
                false
            }
            Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER -> {
                throw IllegalStateException("The [$sink] doesn't have subscribers to accept messages")
            }
            Sinks.EmitResult.FAIL_TERMINATED, Sinks.EmitResult.FAIL_CANCELLED -> {
                throw IllegalStateException("Cannot emit messages into the cancelled or terminated sink: $sink")
            }
            else -> {
//                log.error("Not support ${res.name}")
                throw IllegalStateException("Not support ${res.name}")
            }
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun <T> resultHandler(item: T): Sinks.EmitFailureHandler {
        return Sinks.EmitFailureHandler { _: SignalType, res ->
            when (res) {
                Sinks.EmitResult.FAIL_NON_SERIALIZED -> {
                    LockSupport.parkNanos(10);
                    log.warn("sinks emit failed[$res], and retry after 10 nacos, $item")
                    true
                }
                else -> {
                    log.error("sinks emit failed[$res], $item")
                    false
                }
            }
        }
    }
}
