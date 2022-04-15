package com.rw.random.infra.handler

import com.rw.random.domain.entity.RWTask
import com.rw.random.infra.config.ApplicationProperties
import com.rw.random.infra.subscription.SubscriptionRegistry
import com.rw.random.infra.utils.SinksUtils
import org.slf4j.LoggerFactory
import org.springframework.context.SmartLifecycle
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import reactor.core.scheduler.Schedulers
import reactor.util.concurrent.Queues

@Component
open class TaskHandler(
    applicationProperties: ApplicationProperties
) : SmartLifecycle {

    open val taskHandler: Sinks.Many<RWTask> =
        Sinks.many().unicast().onBackpressureBuffer(Queues.get<RWTask>(applicationProperties.taskChannelSize).get())
    private var running: Boolean = false
    private val log = LoggerFactory.getLogger(javaClass)


    private fun pushTask(task: RWTask) {
        try {
            SinksUtils.tryEmit(taskHandler, task)
        } catch (e: Exception) {
            log.error("push task failed!", e)
        }
    }

    override fun start() {
        running = true
        taskHandler.asFlux()
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe { task ->
                Mono.just(task.run()).subscribe()
                if (!task.isFinish()) {
                    pushTask(task)
                }
            }
    }

    override fun stop() {
        running = false
    }

    override fun isRunning(): Boolean {
        return running
    }


}
