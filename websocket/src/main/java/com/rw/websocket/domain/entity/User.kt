package com.rw.websocket.domain.entity

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("user")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
open class User(
    @Id
    val id: Long,
    @Column("user_name")
    val userName: String,
    val password: String,
    @Column("access_token")
    val accessToken: String?,
)
