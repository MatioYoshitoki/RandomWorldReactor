package com.matio.random.infra.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("random-world")
open class ApplicationProperties {

    var workId: Long = 1

    var dataCenterId: Long = 1

}