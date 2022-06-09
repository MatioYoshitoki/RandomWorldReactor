package com.rw.websocket.infre.exception.handling

import com.rw.websocket.infre.exception.AccessTokenInvalidException
import org.zalando.problem.spring.webflux.advice.SpringAdviceTrait
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.zalando.problem.Problem

interface AccessTokenInvalidExceptionAdviceTrait : SpringAdviceTrait {
    @ExceptionHandler
    fun handleAccessTokenValidException(
        exception: AccessTokenInvalidException,
        request: ServerWebExchange
    ): Mono<ResponseEntity<Problem>> {
        return create(exception.status, exception, request)
    }
}
