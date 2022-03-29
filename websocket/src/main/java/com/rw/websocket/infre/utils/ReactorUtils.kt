package com.rw.websocket.infre.utils

import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

object ReactorUtils {

    fun stringBuffer(value: String): Publisher<ByteBuffer>? {
        return Flux.just(value.toByteArray(StandardCharsets.UTF_8)).map { array: ByteArray? ->
            ByteBuffer.wrap(
                array
            )
        }
    }



}