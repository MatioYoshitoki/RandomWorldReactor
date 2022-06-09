package com.rw.websocket.infre.exception

import org.springframework.http.HttpStatus
import java.lang.IllegalArgumentException

class AccessTokenInvalidException(s: String) : IllegalArgumentException(s) {
    val status = HttpStatus.UNAUTHORIZED
}
