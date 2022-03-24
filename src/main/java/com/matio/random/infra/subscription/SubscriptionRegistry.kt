package com.matio.random.infra.subscription

import com.matio.random.domain.entity.Human
import com.matio.random.domain.entity.RWEvent
import com.matio.random.domain.entity.RWObject
import com.matio.random.domain.entity.RWZone
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import java.util.stream.Stream
import kotlin.math.log

@Component
open class SubscriptionRegistry {

    private val log: Logger = LoggerFactory.getLogger(javaClass)
    private val zoneSubscriptions = ConcurrentHashMap<String, MutableSet<Long>>()
    private val objectHandler = ConcurrentHashMap<Long, Consumer<RWEvent>>()
    private val objectTopic = ConcurrentHashMap<Long, String>()
    private val topicZone = ConcurrentHashMap<String, RWZone>()

    fun subscribe(obj: RWObject, topic: String): Boolean {
        if (zoneSubscriptions.containsKey(topic)) {
            zoneSubscriptions[topic]!!.add(obj.id)
            objectTopic[obj.id] = topic
            objectHandler[obj.id] = obj.handler
            return true
        }
        return false
    }

    fun unsubscribe(obj: RWObject, topic: String) {
        if (zoneSubscriptions.containsKey(topic)) {
            zoneSubscriptions[topic]!!.remove(obj.id)
            objectTopic.remove(obj.id)
            objectHandler.remove(obj.id)
        }
    }

    fun registerZone(zone: RWZone) {
        log.info("Zone created ${zone.zoneName}")
        topicZone[zone.getZoneTopic()] = zone
        zoneSubscriptions[zone.getZoneTopic()] = mutableSetOf()
    }

    fun findAllObjByTopic(topic: String): Flux<Long> {
        return Flux.fromIterable(zoneSubscriptions[topic] ?: setOf())
    }

    fun findConsumerByObjId(objectId: Long): Optional<Consumer<RWEvent>> {
        return Optional.ofNullable(objectHandler[objectId])
    }

    fun findHumanByTopic(topic: String): Stream<Human> {
        if (!topicZone.containsKey(topic)) {
            return Stream.empty()
        }
        return topicZone[topic]!!.getAllObjByType(Human::class).map { it as Human }
    }

}