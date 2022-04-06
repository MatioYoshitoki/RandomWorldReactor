package com.rw.websocket.domain.dto.request

import com.fasterxml.jackson.annotation.JsonProperty

class FishRequest(
    @JsonProperty("fish_id")
    var fishId: Long
)