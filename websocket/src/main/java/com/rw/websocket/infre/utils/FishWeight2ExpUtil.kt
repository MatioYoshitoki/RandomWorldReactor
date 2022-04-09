package com.rw.websocket.infre.utils

object FishWeight2ExpUtil {
    fun exchangeWeight2Exp(weight: Int): Long {
        return weight.toLong() / 1000
    }
}