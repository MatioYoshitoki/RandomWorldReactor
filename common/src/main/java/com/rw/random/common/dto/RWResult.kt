package com.rw.random.common.dto

open class RWResult<T>(
    var errno: Int,
    var message: String,
    var data: T?
) {

    constructor() : this(0, "", null)

    companion object {
        fun <T> success(message: String, data: T?): RWResult<T> {
            return RWResult(0, message, data)
        }

        fun <T> failed(message: String, date: T?): RWResult<T> {
            return RWResult(-1, message, date)
        }
    }
}
