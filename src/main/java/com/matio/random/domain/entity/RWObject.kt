package com.matio.random.domain.entity

import com.matio.random.infra.config.TaskProperties
import com.matio.random.infra.utils.SinksUtils
import org.slf4j.LoggerFactory
import reactor.core.publisher.Sinks
import java.util.*
import java.util.function.Consumer


/**
 * RandomWorld实体基础类
 * 状态的修改都必须通过task来进行。外界无法对每个实体对象的内部状态进行修改
 * */
abstract class RWObject(
    val id: Long,
    val name: String,
    val taskProperties: TaskProperties,
    private val sound: Sinks.Many<RWEvent>?,
    private val taskChannel: Sinks.Many<RWTask>?,
) {

    private val log = LoggerFactory.getLogger(javaClass)
    val taskStack: Stack<RWTask> = Stack()
    var topic: String = ""

    val handler: Consumer<RWEvent> = Consumer { handlerMsg(it) }

    abstract fun handlerMsg(event: RWEvent)

    abstract fun destroy(target: RWObject?)

    abstract fun subscribe(topic: String)

    abstract fun unsubscribe(topic: String)

    fun sendMsg(event: RWEvent) {
        if (sound != null) {
            try {
                SinksUtils.tryEmit(sound, event)
            } catch (e: Exception) {
                log.error("send msg failed!", e)
            }
        }
    }

    fun pushTask(task: RWTask) {
        if (taskChannel != null) {
            try {
                SinksUtils.tryEmit(taskChannel, task)
            } catch (e: Exception) {
                log.error("push task failed!", e)
            }
        }
    }

}