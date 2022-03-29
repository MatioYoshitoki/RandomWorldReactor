package com.rw.random.api.controller

import com.rw.random.app.usecase.EnterHumanUseCase
import com.rw.random.domain.dto.RWResult
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v1/api/human")
open class HumanEnterController(
    private val humanUseCase: EnterHumanUseCase
) {

    @PostMapping("/enter")
    fun enter(): Mono<RWResult<String>> {
        return humanUseCase.runCase()
            .map {
                if (it) {
                    RWResult.success("Enter Success!", null)
                } else {
                    RWResult.failed("Too Many!", null)
                }
            }
    }

}