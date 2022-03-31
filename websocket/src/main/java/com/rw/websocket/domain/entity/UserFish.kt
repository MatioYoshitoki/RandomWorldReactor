package com.rw.websocket.domain.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.Date

@Table("user_fish")
open class UserFish(
    @Column("user_id")
    val userId: Long,
    @Column("fish_id")
    val fishId: Long,
    @Column("fish_status")
    val fishStatus: Int,
    @Column("create_time")
    val createTime: Date
) {

}