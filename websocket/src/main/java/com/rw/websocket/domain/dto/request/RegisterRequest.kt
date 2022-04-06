package com.rw.websocket.domain.dto.request

class RegisterRequest(
    var userName: String = "",
    var password: String = "",
    var passwordAgain: String = ""
)