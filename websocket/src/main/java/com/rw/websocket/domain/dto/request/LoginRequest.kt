package com.rw.websocket.domain.dto.request

import com.fasterxml.jackson.annotation.JsonProperty

class LoginRequest(
    @JsonProperty("user_name")
    var userName: String = "",
    @JsonProperty("password")
    var password: String = ""
)