package com.rw.websocket.infre.contants

import java.net.URI

object ErrorConstants {
    const val ERR_VALIDATION = "error.validation"
    private const val PROBLEM_BASE_URL = "https://xxxx/cloud"

    @JvmField
    val DEFAULT_TYPE: URI = URI.create("$PROBLEM_BASE_URL/problem-with-message")

    @JvmField
    val CONSTRAINT_VIOLATION_TYPE: URI = URI.create("$PROBLEM_BASE_URL/constraint-violation")
}
