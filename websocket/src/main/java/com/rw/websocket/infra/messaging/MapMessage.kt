package com.rw.websocket.infra.messaging

import org.springframework.lang.NonNull
import java.util.*

class MapMessage(map: Map<String, Any>) : HashMap<String, Any>() {
    init {
        super.putAll(map)
    }

    fun getAsString(key: String): String? {
        return Optional.ofNullable(this[key]).map { obj: Any -> obj.toString() }.orElse(null)
    }

    fun getAsLong(key: String): Long? {
        return Optional.ofNullable(this[key]).map { obj: Any -> obj.toString() }
            .map { s: String? -> java.lang.Long.valueOf(s) }.orElse(null)
    }

    fun getAsBoolean(key: String): Boolean? {
        return Optional.ofNullable(this[key]).map { obj: Any -> obj.toString() }
            .map { s: String? -> java.lang.Boolean.valueOf(s) }.orElse(null)
    }

    @NonNull
    fun getOrDefaultAsString(key: String, defaultValue: String?): String {
        return getOrDefault(key, defaultValue).toString()
    }

    @NonNull
    fun getOrDefaultAsLong(key: String?, defaultValue: Long?): Long {
        return java.lang.Long.valueOf(getOrDefault(key, defaultValue).toString())
    }

    operator fun set(key: String, value: Any) {
        put(key, value)
    }

    fun asMap(): Map<String, Any> {
        return Collections.unmodifiableMap(this)
    }

    fun copyTo(): Map<String, Any> {
        return HashMap(this)
    }

    override fun toString(): String {
        return super.toString()
    }
}
