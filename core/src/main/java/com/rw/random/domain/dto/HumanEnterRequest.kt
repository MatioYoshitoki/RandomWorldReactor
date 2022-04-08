package com.rw.random.domain.dto

import com.fasterxml.jackson.annotation.JsonProperty

class FishIdRequest(
    @JsonProperty("fish_id")
    val fishId: Long
)