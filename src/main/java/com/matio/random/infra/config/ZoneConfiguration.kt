package com.matio.random.infra.config

import com.matio.random.domain.entity.RWZone
import com.matio.random.domain.entity.SimpleZone
import com.matio.random.infra.subscription.SubscriptionRegistry
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(ApplicationProperties::class)
open class ZoneConfiguration(
    private val applicationProperties: ApplicationProperties
) {

    @Bean
    open fun simpleZone(subscriptionRegistry: SubscriptionRegistry): RWZone {
        val zone = SimpleZone(
            762136123,
            applicationProperties.zoneName,
            applicationProperties.zoneMoney,
            applicationProperties.zoneMoneyIncrSpeed,
            subscriptionRegistry
        )
        subscriptionRegistry.registerZone(zone)
        return zone
    }

}