package com.rw.websocket.infra.utils

import com.rw.random.common.constants.BeingStatus

object FishWeight2ExpUtil {
    fun exchangeWeight2Exp(weight: Int, fishStatus: Int): Long {
        return when (fishStatus) {
            BeingStatus.SLEEP.ordinal -> weight.toLong() / 1000
            BeingStatus.DEAD.ordinal -> weight.toLong() / 5000
            else -> 0
        }
    }
}
