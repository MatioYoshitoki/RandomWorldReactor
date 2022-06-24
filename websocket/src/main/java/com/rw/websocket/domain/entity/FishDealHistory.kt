package com.rw.websocket.domain.entity

import cn.hutool.core.date.DateUtil
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.sql.Date

@Table("fish_sell_log")
open class FishDealHistory(
    @Id
    var id: Long? = null,
    @Column("order_id")
    val orderId: Long,
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
    val createTime: Date,
    @Column("update_time")
    val updateTime: Date
) {
    companion object {
        fun of(fishSellLog: FishSellLog, buyerId: Long, buyerName: String): FishDealHistory {
            return FishDealHistory(
                null,
                fishSellLog.id!!,
                fishSellLog.sellerId,
                fishSellLog.sellerName,
                buyerId,
                buyerName,
                fishSellLog.fishId,
                fishSellLog.fishName,
                fishSellLog.fishDetail,
                fishSellLog.price,
                DateUtil.date().toSqlDate(),
                DateUtil.date().toSqlDate(),
            )
        }
    }
}
