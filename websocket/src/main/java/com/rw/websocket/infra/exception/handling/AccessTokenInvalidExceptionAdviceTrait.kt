package com.rw.websocket.infra.exception.handling

import com.rw.websocket.infra.exception.AccessTokenInvalidException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.server.ServerWebExchange
import org.zalando.problem.Problem
import org.zalando.problem.spring.webflux.advice.SpringAdviceTrait
import reactor.core.publisher.Mono

interface AccessTokenInvalidExceptionAdviceTrait : SpringAdviceTrait {
    @ExceptionHandler
    fun handleAccessTokenValidException(
        exception: AccessTokenInvalidException,
        request: ServerWebExchange
    ): Mono<ResponseEntity<Problem>> {
        return create(exception.status, exception, request)
    }
}
