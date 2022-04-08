package com.rw.random.api.controller

import com.rw.random.app.usecase.EnterObjectUseCase
import com.rw.random.app.usecase.OutObjectUseCase
import com.rw.random.app.usecase.PutObjectUseCase
import com.rw.random.common.dto.RWResult
import com.rw.random.domain.dto.FishIdRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/object")
open class ObjectController(
    private val enterObjectUseCase: EnterObjectUseCase,
    private val outObjectUseCase: OutObjectUseCase,
    private val putObjectUseCase: PutObjectUseCase
) {

    @PostMapping("/enter")
    fun enter(): Mono<RWResult<Long>> {
        return enterObjectUseCase.runCase()
            .map {
                RWResult.success("Enter Success!", it)
            }
            .defaultIfEmpty(RWResult.failed("Too Many!", 0))
    }

    @PostMapping("/out")
    fun out(@RequestBody request: FishIdRequest): Mono<RWResult<Long>> {
        return outObjectUseCase.runCase(request.fishId)
            .map {
                RWResult.success("成功", it)
            }
            .defaultIfEmpty(RWResult.failed("Fish Not Exist!", null))
    }

    @PostMapping("/put")
    fun put(@RequestBody request: FishIdRequest): Mono<RWResult<Long>> {
        return putObjectUseCase.runCase(request.fishId)
            .map {
                RWResult.success("成功", it)
            }
            .defaultIfEmpty(RWResult.failed("Fish Not Exist!", null))
    }

}