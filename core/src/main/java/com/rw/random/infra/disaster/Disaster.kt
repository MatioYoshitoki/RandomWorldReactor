package com.rw.random.infra.disaster

import com.rw.random.domain.entity.obj.Fish

interface Disaster {

    fun survive(fish: Fish): Boolean

    fun suffer(fish: Fish)

}
