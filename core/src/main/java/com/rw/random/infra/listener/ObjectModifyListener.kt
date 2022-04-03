package com.rw.random.infra.listener

import com.rw.random.common.constants.BeingStatus
import com.rw.random.common.entity.UserFish
import com.rw.random.domain.repository.UserFishRepository
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEvent
import org.springframework.context.event.EventListener
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.query.Criteria.where
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
open class ObjectModifyListener(
    private val userFishRepository: UserFishRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener
    @Async
    fun objectStatusModifyListener(event: ObjectStatusModifyEvent){
        val sourceId = event.payload.sourceId
        val status = event.payload.status
        log.info("Update object status [sourceId=$sourceId, status=$status]")
        userFishRepository.updateStatus(sourceId, status)
            .subscribe()
    }

}

open class ObjectStatusModifyEvent(
    val payload: ObjectStatusModifyEventPayload
): ApplicationEvent(payload) {
    class ObjectStatusModifyEventPayload(
        val sourceId: Long,
        val status: BeingStatus
    )
    companion object {
        fun of(sourceId: Long, status: BeingStatus): ObjectStatusModifyEvent{
            return ObjectStatusModifyEvent(ObjectStatusModifyEventPayload(sourceId, status))
        }
    }

}