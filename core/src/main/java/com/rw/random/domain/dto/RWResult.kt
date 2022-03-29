package com.rw.random.domain.dto

open class RWResult<T>(
    val errno: Int,
    val message: String,
    val data: T?
) {

    companion object {
        fun <T> success(message: String, data: T?): RWResult<T> {
            return RWResult(0, message, data)
        }

        fun <T> failed(message: String, date: T?): RWResult<T> {
            return RWResult(-1, message, null)
        }
    }
}
