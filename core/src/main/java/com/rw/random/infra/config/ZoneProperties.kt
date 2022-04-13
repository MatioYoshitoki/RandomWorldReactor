package com.rw.random.infra.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "random-world.zone")
open class ZoneProperties {

    val maxObjSize: Long = 10000

    val zoneMoney: Long = 1000000

    val zoneMoneyIncrSpeed: Long = 5000

    val zoneName: String = "鱼缸"

}

