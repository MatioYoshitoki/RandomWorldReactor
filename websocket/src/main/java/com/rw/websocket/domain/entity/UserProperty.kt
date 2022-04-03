package com.rw.websocket.domain.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("user_property")
open class UserProperty(
    @Column("id")
    var id: Long? = null,
    @Column("exp")
    var exp: Long? = null,
    @Column("money")
    var money: Long? = null,
    @Column("fish_max_count")
    var fishMaxCount: Long? = null,
)