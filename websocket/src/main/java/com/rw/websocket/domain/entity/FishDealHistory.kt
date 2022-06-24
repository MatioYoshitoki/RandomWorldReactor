package com.rw.websocket.domain.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("fish_sell_log")
open class FishDealHistory(
    @Id
    val id: Long,
    @Column("seller_id")
    val sellerId: Long,
    @Column("seller_name")
    val sellerName: String,
    @Column("buyer_id")
    val buyerId: Long,
    @Column("buyer_name")
    val buyerName: String,
    @Column("fish_id")
    val fishId: Long,
    @Column("fish_name")
    val fishName: String,
    @Column("fish_detail")
    val fishDetail: String,
    @Column("price")
    val price: Long,
    @Column("create_time")
    val createTime: Long,
    @Column("update_time")
    val updateTime: Long
)
