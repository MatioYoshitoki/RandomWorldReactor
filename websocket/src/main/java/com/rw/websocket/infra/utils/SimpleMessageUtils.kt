package com.rw.websocket.infra.utils

import org.springframework.lang.Nullable
import org.springframework.messaging.Message

object SimpleMessageUtils {
    const val ACTION_TYPE_HEADER = "actionType"
    const val SESSION_ID_HEADER = "sessionId"
    const val PAYLOAD_BYTES = "payload_bytes"
    const val DESTINATION_HEADER = "destination"
    const val REMOTE_IP_HEADER = "remoteIp"
    const val USER_ID_HEADER = "userId"
    const val APPLICATION_DESTINATION_PREFIX = "/app/"
    const val SESSION_DESTINATION_PREFIX = "/topic/session/"
    const val USER_DESTINATION_PREFIX = "/topic/user/"
    const val WORLD_DESTINATION_PREFIX = "/topic/world"
    const val OWNER_DESTINATION_PREFIX = "/topic/owner/"

    const val FIELD_USER_ID = "user_id"

    fun getDestination(message: Message<*>, defaultValue: String?): String? {
        val v = message.headers[DESTINATION_HEADER]
        return v?.toString() ?: defaultValue
    }

    fun getActionType(message: Message<*>, defaultValue: String): String {
        val v = message.headers[ACTION_TYPE_HEADER]
        return v?.toString() ?: defaultValue
    }

    fun getActionType(message: Message<*>): String {
        return getActionType(message, "null")
    }

    fun getRemoteIp(message: Message<*>): String? {
        val v = message.headers[REMOTE_IP_HEADER]
        return v?.toString()
    }

    @Nullable
    fun getSessionId(message: Message<*>): String? {
        val v = message.headers[SESSION_ID_HEADER]
        return v?.toString()
    }

    @Nullable
    fun getUserId(message: Message<*>): Long? {
        val v = message.headers[USER_ID_HEADER]?.toString()
        return v?.toLongOrNull()
    }

    fun buildAppDestination(actionType: String): String {
        return APPLICATION_DESTINATION_PREFIX + actionType
    }

    fun buildUserDestination(userId: Long?): String {
        return USER_DESTINATION_PREFIX + userId
    }

    fun buildSessionDestination(sessionId: String?): String {
        return SESSION_DESTINATION_PREFIX + sessionId
    }

    fun buildWorldDestination(): String {
        return WORLD_DESTINATION_PREFIX
    }

    fun isOwnerDestination(dest: String): Boolean {
        return dest.startsWith(OWNER_DESTINATION_PREFIX)
    }

    fun legalDestination(dest: String?): Boolean {
        if (dest.isNullOrBlank()) {
            return false
        }
        if (dest.startsWith(USER_DESTINATION_PREFIX) || dest.startsWith(SESSION_DESTINATION_PREFIX) || dest.startsWith(
                WORLD_DESTINATION_PREFIX
            ) || dest.startsWith(OWNER_DESTINATION_PREFIX)
        ) {
            return true
        }
        return false
    }

}
