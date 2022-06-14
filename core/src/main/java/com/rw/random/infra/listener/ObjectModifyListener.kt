package com.rw.random.infra.listener

import com.rw.random.common.constants.BeingStatus
import com.rw.random.domain.repository.UserFishRepository
import com.rw.random.domain.service.PersistenceService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
open class ObjectModifyListener(
    private val userFishRepository: UserFishRepository,
    private val persistenceService: PersistenceService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener
    @Async
    open fun objectStatusModifyListener(event: ObjectStatusModifyEvent) {
        val sourceId = event.payload.sourceId
        val status = event.payload.status
        val hasMaster = event.payload.hasMaster
        log.info("Update object status [sourceId=$sourceId, status=$status]")
        persistenceService.loadFish(sourceId)
            .map {
                it.status = status
                it
            }
            .flatMap {
                persistenceService.persistenceFish(it)
            }
            .flatMap {
                if (hasMaster) {
                    userFishRepository.updateStatus(sourceId, status)
                } else {
                    Mono.empty()
                }
            }
            .subscribe()
    }

}

open class ObjectStatusModifyEvent(
    val payload: ObjectStatusModifyEventPayload
) : ApplicationEvent(payload) {
    class ObjectStatusModifyEventPayload(
        val sourceId: Long,
        val status: BeingStatus,
        val hasMaster: Boolean,
    )

    companion object {
        fun of(sourceId: Long, status: BeingStatus, hasMaster: Boolean): ObjectStatusModifyEvent {
            return ObjectStatusModifyEvent(ObjectStatusModifyEventPayload(sourceId, status, hasMaster))
        }
    }

}
