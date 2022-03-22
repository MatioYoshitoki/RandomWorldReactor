package com.matio.random.infra.subscription

import com.matio.random.domain.entity.Human
import com.matio.random.domain.entity.RWEvent
import com.matio.random.domain.entity.RWObject
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import java.util.stream.Stream

@Component
open class SubscriptionRegistry {

    private val zoneSubscriptions = ConcurrentHashMap<String, MutableSet<Long>>()
    private val objectHandler = ConcurrentHashMap<Long, Consumer<RWEvent>>()
    private val objectTopic = ConcurrentHashMap<Long, String>()
    private val objectSet = mutableSetOf<RWObject>()


    init {
        // 通过配置文件的方式进行默认topic的初始化
        zoneSubscriptions["world"] = mutableSetOf()
    }

    fun subscribe(obj: RWObject, topic: String) {
        if (zoneSubscriptions.containsKey(topic)) {
            zoneSubscriptions[topic]!!.add(obj.id)
            objectTopic[obj.id] = topic
            objectHandler[obj.id] = obj.handler
            objectSet.add(obj)
        }
    }

    fun unsubscribe(obj: RWObject, topic: String) {
        if (zoneSubscriptions.containsKey(topic)) {
            zoneSubscriptions[topic]!!.remove(obj.id)
            objectTopic.remove(obj.id)
            objectHandler.remove(obj.id)
            objectSet.remove(obj)
        }
    }

    fun findAllObjByTopic(topic: String): Flux<Long> {
        return Flux.fromIterable(zoneSubscriptions[topic] ?: setOf())
    }

    fun findConsumerByObjId(objectId: Long): Optional<Consumer<RWEvent>> {
        return Optional.ofNullable(objectHandler[objectId])
    }

    fun findHumanByZone(zoneId: Long): Stream<Human> {
        return objectSet.stream().filter { it is Human }.map { it as Human }
    }

}