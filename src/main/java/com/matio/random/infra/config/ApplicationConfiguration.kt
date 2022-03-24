package com.matio.random.infra.config

import cn.hutool.core.lang.Snowflake
import cn.hutool.core.util.IdUtil
import com.matio.random.domain.entity.RWZone
import com.matio.random.domain.entity.SimpleZone
import com.matio.random.infra.subscription.SubscriptionRegistry
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    ZoneProperties::class,
    ApplicationProperties::class,
    TaskProperties::class,
    ObjectProperties::class
)
open class ApplicationConfiguration(
    private val zoneProperties: ZoneProperties,
    private val applicationProperties: ApplicationProperties
) {

    @Bean
    open fun simpleZone(subscriptionRegistry: SubscriptionRegistry): RWZone {
        val zone = SimpleZone(
            762136123,
            zoneProperties.zoneName,
            zoneProperties.zoneMoney,
            zoneProperties.zoneMoneyIncrSpeed,
            subscriptionRegistry
        )
        subscriptionRegistry.registerZone(zone)
        return zone
    }

    @Bean
    open fun snowFlake(): Snowflake {
        return IdUtil.getSnowflake(applicationProperties.workId, applicationProperties.dataCenterId)
    }

}