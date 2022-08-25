package com.rw.websocket.domain.entity

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("fish_sold_out_log")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
open class FishSoldOutLog(
    @Id
    val id: Long,
    @Column("order_id")
    val orderId: Long,
    @Column("create_time")
    val createTime: Long,
    @Column("update_time")
    val updateTime: Long
)
