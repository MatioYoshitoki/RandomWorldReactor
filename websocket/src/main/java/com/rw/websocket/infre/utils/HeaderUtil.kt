package com.rw.websocket.infre.utils

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Utility class for HTTP headers creation.
 */
object HeaderUtil {
    private val log = LoggerFactory.getLogger(HeaderUtil::class.java)

    /**
     *
     * createAlert.
     *
     * @param applicationName a [String] object.
     * @param message         a [String] object.
     * @param param           a [String] object.
     * @return a [HttpHeaders] object.
     */
    fun createAlert(applicationName: String, message: String?, param: String?): HttpHeaders {
        val headers = HttpHeaders()
        headers.add("X-$applicationName-alert", message)
        try {
            headers.add("X-$applicationName-params", URLEncoder.encode(param, StandardCharsets.UTF_8.toString()))
        } catch (e: UnsupportedEncodingException) {
            // StandardCharsets are supported by every Java implementation so this exception will never happen
        }
        return headers
    }

    /**
     *
     * createEntityCreationAlert.
     *
     * @param applicationName   a [String] object.
     * @param enableTranslation a boolean.
     * @param entityName        a [String] object.
     * @param param             a [String] object.
     * @return a [HttpHeaders] object.
     */
    fun createEntityCreationAlert(
        applicationName: String,
        enableTranslation: Boolean,
        entityName: String,
        param: String
    ): HttpHeaders {
        val message =
            if (enableTranslation) "$applicationName.$entityName.created" else "A new $entityName is created with identifier $param"
        return createAlert(applicationName, message, param)
    }

    /**
     *
     * createEntityUpdateAlert.
     *
     * @param applicationName   a [String] object.
     * @param enableTranslation a boolean.
     * @param entityName        a [String] object.
     * @param param             a [String] object.
     * @return a [HttpHeaders] object.
     */
    fun createEntityUpdateAlert(
        applicationName: String,
        enableTranslation: Boolean,
        entityName: String,
        param: String
    ): HttpHeaders {
        val message =
            if (enableTranslation) "$applicationName.$entityName.updated" else "A $entityName is updated with identifier $param"
        return createAlert(applicationName, message, param)
    }

    /**
     *
     * createEntityDeletionAlert.
     *
     * @param applicationName   a [String] object.
     * @param enableTranslation a boolean.
     * @param entityName        a [String] object.
     * @param param             a [String] object.
     * @return a [HttpHeaders] object.
     */
    fun createEntityDeletionAlert(
        applicationName: String,
        enableTranslation: Boolean,
        entityName: String,
        param: String
    ): HttpHeaders {
        val message =
            if (enableTranslation) "$applicationName.$entityName.deleted" else "A $entityName is deleted with identifier $param"
        return createAlert(applicationName, message, param)
    }

    /**
     *
     * createFailureAlert.
     *
     * @param applicationName   a [String] object.
     * @param enableTranslation a boolean.
     * @param entityName        a [String] object.
     * @param errorKey          a [String] object.
     * @param defaultMessage    a [String] object.
     * @return a [HttpHeaders] object.
     */
    fun createFailureAlert(
        applicationName: String,
        enableTranslation: Boolean,
        entityName: String?,
        errorKey: String,
        defaultMessage: String?
    ): HttpHeaders {
        log.error("Entity processing failed, {}", defaultMessage)
        val message = if (enableTranslation) "error.$errorKey" else defaultMessage!!
        val headers = HttpHeaders()
        headers.add("X-$applicationName-error", message)
        headers.add("X-$applicationName-params", entityName)
        return headers
    }
}
