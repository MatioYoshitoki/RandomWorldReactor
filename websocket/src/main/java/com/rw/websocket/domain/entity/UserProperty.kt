package com.rw.websocket.domain.entity

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("user_property")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
open class UserProperty(
    @Column("id")
    @Id
    var id: Long? = null,
    @Column("exp")
    var exp: Long? = null,
    @Column("money")
    var money: Long? = null,
    @Column("fish_max_count")
    var fishMaxCount: Long? = null,
)
