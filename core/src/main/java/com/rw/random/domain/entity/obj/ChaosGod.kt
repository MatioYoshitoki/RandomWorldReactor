package com.rw.random.domain.entity.obj

import com.rw.random.infra.config.TaskProperties

class ChaosGod(
    name: String,
    taskProperties: TaskProperties
) : Being(
    id = 999999,
    name,
    false,
    null,
    Int.MAX_VALUE,
    Int.MAX_VALUE,
    taskProperties,
    null,
    null
)
