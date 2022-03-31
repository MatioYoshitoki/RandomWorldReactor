package com.rw.websocket.domain.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("user")
open class User(
    val id: Long,
    @Column("user_name")
    val userName: String,
    val password: String,
    @Column("access_token")
    val accessToken: String,
)