package com.rw.random.common.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.sql.Date

@Table("user_fish")
open class UserFish(
    @Column("user_id")
    val userId: Long,
    @Column("fish_id")
    val fishId: Long,
    @Column("fish_status")
    val fishStatus: Int,
    @Column("create_time")
    var createTime: Date? = null
) {

}