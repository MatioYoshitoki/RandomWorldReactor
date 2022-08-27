package com.rw.websocket.domain.dto.request

open class PageRequest(
    var pageNo: Int = 1,
    var pageSize: Int = 20,
    var orderBy: String = "create_time",
    var sort: String = "desc"
)

open class FishSellListRequest(
    var orderId: Long?,
    pageNo: Int = 1,
    pageSize: Int = 20,
    orderBy: String = "create_time",
    sort: String = "desc"
) : PageRequest(pageNo, pageSize, orderBy, sort)
