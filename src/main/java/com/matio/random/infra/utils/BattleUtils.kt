package com.matio.random.infra.utils

object BattleUtils {

    private const val A: Double = 1.0
    private const val B: Double = 4500.0
    private const val C: Double = 4500.0

    fun defRate(def: Int): Double {
        return A - (B / (def + C))
    }

}