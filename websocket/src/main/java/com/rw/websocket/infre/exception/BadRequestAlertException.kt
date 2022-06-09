package com.rw.websocket.infre.exception

import org.zalando.problem.AbstractThrowableProblem
import org.zalando.problem.Exceptional
import org.zalando.problem.Status
import java.net.URI

open class BadRequestAlertException(
    type: URI,
    defaultMessage: String,
    val entityName: String,
    val errorKey: String
) :
    AbstractThrowableProblem(
        type,
        defaultMessage,
        Status.BAD_REQUEST,
        null,
        null,
        null,
        getAlertParameters(entityName, errorKey)
    ) {

    companion object {
        private const val serialVersionUID = 1L
        private fun getAlertParameters(entityName: String, errorKey: String): Map<String, Any> {
            val parameters: MutableMap<String, Any> = HashMap()
            parameters["message"] = "error.$errorKey"
            parameters["params"] = entityName
            return parameters
        }
    }

    override fun getCause(): Exceptional {
        return super.cause as Exceptional
    }


}
