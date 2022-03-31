package com.rw.random.api.controller

import com.rw.random.app.usecase.EnterObjectUseCase
import com.rw.random.common.dto.RWResult
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v1/api/object")
open class ObjectEnterController(
    private val humanUseCase: EnterObjectUseCase
) {

    @PostMapping("/enter")
    fun enter(): Mono<RWResult<Long>> {
        return humanUseCase.runCase()
            .map {
                RWResult.success("Enter Success!", it)
            }
            .defaultIfEmpty(RWResult.failed("Too Many!", 0))
    }

}