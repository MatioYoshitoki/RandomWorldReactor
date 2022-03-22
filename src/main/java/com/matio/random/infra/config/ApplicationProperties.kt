package com.matio.random.infra.config

import com.matio.random.domain.entity.ATKTask
import com.matio.random.domain.entity.EarnTask
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "random")
open class ApplicationProperties {

    //    val task: Task = Task()
    val countDown = mapOf(EarnTask::class.simpleName!! to 3000, ATKTask::class.simpleName!! to 500)
//    open class Task() {
//        val countDown = mapOf(EarnTask::class.simpleName!! to 3000, ATKTask::class.simpleName!! to 500)
//    }

}