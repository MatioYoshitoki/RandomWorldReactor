package com.matio.random.infra.config

import com.matio.random.domain.entity.ATKTask
import com.matio.random.domain.entity.EarnTask
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "randoms")
open class ApplicationProperties {


    val countDown = mapOf(EarnTask::class.simpleName!! to 3000, ATKTask::class.simpleName!! to 500)

    val zoneMoney: Long = 1000000

    val zoneMoneyIncrSpeed: Long = 5000

    val zoneName: String = "鱼缸"

}