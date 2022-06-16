package com.rw.websocket.infre.subscription

import cn.hutool.core.collection.ConcurrentHashSet
import org.agrona.collections.LongHashSet
import org.agrona.collections.Object2ObjectHashMap
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

typealias TopicPattern = String

@Component
open class SubscriptionRegistry {

    private val subscriptionLock = ReentrantLock()
    private val topicSubscriptions = Object2ObjectHashMap<String, LongHashSet>()
    private val subscriptions = IdentifiableSet<Subscription>(RandomIdGenerator())
    private val sessionSubscriptions = Object2ObjectHashMap<String, LongHashSet>()
    private val topicSessionSet = ConcurrentHashMap<TopicPattern, MutableSet<String>>()

    private val log = LoggerFactory.getLogger(javaClass)

    fun subscribe(sessionId: String, topic: TopicPattern): Long {
        val id = findExistingSubscription(sessionId, topic)
            ?: newSubscription(
                sessionId,
                topic
            ).subscriptionId
        topicSessionSet.computeIfAbsent(topic) { ConcurrentHashSet() }.add(sessionId)
        log.info("sessionId=$sessionId subscribed topic $topic")
//        cache.refresh(Tuples.of(sessionId, topic))
        return id
    }

    private fun findExistingSubscription(sessionId: String, topic: TopicPattern) =
        subscriptionLock.withLock {
            topicSubscriptions[topic]?.find { subscriptions[it]?.sessionId == sessionId }
        }

    private fun newSubscription(sessionId: String, topic: TopicPattern) =

        subscriptionLock.withLock {
            subscriptions.putWithId { subscriptionId ->
                sessionSubscriptions.computeIfAbsent(sessionId) { LongHashSet() }.add(subscriptionId)
                topicSubscriptions.computeIfAbsent(topic) { LongHashSet() } += subscriptionId
                Subscription(topic, sessionId, subscriptionId)
            }
        }

    fun unsubscribe(sessionId: String, topic: TopicPattern) {
        val subscriptionId = sessionSubscriptions[sessionId]?.find { subscriptions[it]?.topic == topic } ?: return
        sessionSubscriptions[sessionId]?.remove(subscriptionId)
        if (sessionSubscriptions[sessionId]?.isEmpty() == true) {
            sessionSubscriptions.remove(sessionId)
        }

        removeSubscription(subscriptionId)
    }

    private fun removeSubscription(subscriptionId: Long) {
        subscriptionLock.withLock {
            val subscription = subscriptions.remove(subscriptionId) ?: return@withLock
            topicSubscriptions[subscription.topic]?.removeIf(subscriptionId::equals)
            if (topicSubscriptions[subscription.topic]?.isEmpty() == true) {
                topicSubscriptions.remove(subscription.topic)
            }

            topicSessionSet[subscription.topic]?.remove(subscription.sessionId)
            if (topicSessionSet[subscription.topic]?.isEmpty() == true) {
                topicSessionSet.remove(subscription.topic)
            }
            log.info("sessionId=${subscription.sessionId} unsubscribed topic ${subscription.topic}")
        }
    }

    fun getSubscriptions(topic: TopicPattern) =
        subscriptionLock.withLock {
            getTopicSubscriptions(topic)
        }


    fun getSubscriptionCount(topic: TopicPattern) =
        subscriptionLock.withLock {
            getTopicSubscriptions(topic).count()
        }

    fun getSubscriptionSessions(topic: TopicPattern): List<String> {
        return topicSessionSet[topic]?.toList() ?: listOf()
    }

    fun isSubscribed(sessionId: String, topic: TopicPattern /* = kotlin.String */): Boolean {
//        return cache.get(Tuples.of(sessionId, topic)) ?: false
        return isSubscribedInternal(sessionId, topic)
    }

    private fun isSubscribedInternal(sessionId: String, topic: TopicPattern /* = kotlin.String */): Boolean {
        return sessionSubscriptions[sessionId]?.any { subscriptions[it]?.topic == topic } ?: false
    }

    private fun getTopicSubscriptions(topic: TopicPattern): List<Subscription> {
        return topicSubscriptions[topic]?.map { subscriptionId ->
            subscriptions[subscriptionId]!!
        } ?: emptyList()
    }

    fun cleanSessionResources(sessionId: String) {
        sessionSubscriptions.remove(sessionId)?.forEach { subscriptionId ->
            removeSubscription(subscriptionId)
        }
    }

}

data class Subscription(
    val topic: TopicPattern,
    val sessionId: String,
    val subscriptionId: Long
)
