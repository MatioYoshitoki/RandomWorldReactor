package com.rw.random.api.controller

import com.rw.random.app.usecase.EnterObjectUseCase
import com.rw.random.app.usecase.OutObjectUseCase
import com.rw.random.app.usecase.PutObjectUseCase
import com.rw.random.common.dto.RWResult
import com.rw.random.domain.dto.FishIdRequest
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.time.Month

@RestController
@RequestMapping("/api/v1/object")
open class ObjectController(
    private val enterObjectUseCase: EnterObjectUseCase,
    private val outObjectUseCase: OutObjectUseCase,
    private val putObjectUseCase: PutObjectUseCase
) {

    private val log = LoggerFactory.getLogger(this::class.java)
    @PostMapping("/enter")
    fun enter(@RequestParam("master_id") masterId: Long?): Mono<RWResult<Long>> {
        return enterObjectUseCase.runCase(masterId)
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
        log.info("try to put fish[fishId=${request.fishId}]")
        return putObjectUseCase.runCase(request.fishId)
            .map {
                RWResult.success("成功", it)
            }
            .onErrorResume {
                Mono.just(RWResult.failed(it.message?:"", -1L))
            }
            .defaultIfEmpty(RWResult.failed("Fish Not Exist!", null))
    }

}
