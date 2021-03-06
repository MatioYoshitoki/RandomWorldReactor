package com.matio.random.infra.config

import com.matio.random.domain.entity.ATKTask
import com.matio.random.domain.entity.EarnTask
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("random-world.task")
open class TaskProperties {

    val countDown = mapOf(EarnTask::class.simpleName!! to 3000, ATKTask::class.simpleName!! to 500)


}