package com.rw.websocket.domain.entity

import cn.hutool.core.date.DateUtil
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.rw.websocket.domain.dto.request.FishDetails
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.sql.Date

@Table("fish_sell_log")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
open class FishSellLog(
    @Id
    var id: String? = null,
    @Column("seller_id")
    val sellerId: String,
    @Column("seller_name")
    val sellerName: String,
    @Column("fish_id")
    val fishId: String,
    @Column("fish_name")
    val fishName: String,
    @Column("fish_detail")
    val fishDetail: String,
    @Column("price")
    val price: Int,
    @Column("status")
    val status: Int,
    @Column("create_time")
    val createTime: Date,
    @Column("update_time")
    val updateTime: Date
) {
    companion object {
        fun of(
            sellerId: String,
            sellerName: String,
            fishId: String,
            fishName: String,
            fishDetail: FishDetails,
            price: Int,
        ): FishSellLog {
            val nowDt = DateUtil.date().toSqlDate()
            return FishSellLog(
                null,
                sellerId,
                sellerName,
                fishId,
                fishName,
                fishDetail.toString(),
                price,
                0,
                nowDt,
                nowDt
            )
        }
    }
}
