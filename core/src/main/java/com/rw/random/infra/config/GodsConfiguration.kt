package com.rw.random.infra.config

import com.rw.random.domain.entity.obj.ChaosGod
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class GodsConfiguration(
    private val taskProperties: TaskProperties
) {
    @Bean
    open fun crazyGod(): ChaosGod {
        return ChaosGod("疯魔", taskProperties)
    }

    @Bean
    open fun thunderGod(): ChaosGod {
        return ChaosGod("雷神", taskProperties)
    }

}
