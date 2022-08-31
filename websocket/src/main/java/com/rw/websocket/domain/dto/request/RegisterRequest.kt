package com.rw.websocket.domain.dto.request

import com.fasterxml.jackson.annotation.JsonProperty

class RegisterRequest(
    @JsonProperty("user_name")
    var userName: String = "",
    @JsonProperty("password")
    var password: String = "",
    @JsonProperty("password_again")
    var passwordAgain: String = ""
)