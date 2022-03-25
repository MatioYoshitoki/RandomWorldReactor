package com.matio.random.domain.entity

import com.matio.random.domain.entity.obj.Being

abstract class RWTask(
    private val stepSize: Int,
    val source: RWObject,
    val target: RWObject?
) {
    var step = 0

    fun isFinish(): Boolean {
        return step <= stepSize
    }

    abstract fun run()

    fun nextStep(): Boolean {
        if (isFinish()) {
            return true
        }
        step++
        return isFinish()
    }

    fun finish() {
        this.step = this.stepSize
    }

}

open class UpgradeWeaponTask(
    stepSize: Int,
    source: Being,
) : RWTask(stepSize, source, null) {
    override fun run() {
        if ((source as Being).cost(300)) {
            source.sendMsg(
                UpgradeWeaponEvent(
                    System.currentTimeMillis(),
                    "UpgradeWeapon",
                    source.topic,
                    200,
                    source
                )
            )
        }

        this.finish()
    }
}

open class HealTask(
    stepSize: Int,
    source: Being,
) : RWTask(stepSize, source, null) {
    override fun run() {
        if ((source as Being).cost(300)) {
            source.sendMsg(
                HealEvent(
                    System.currentTimeMillis(),
                    "Heal",
                    source.topic,
                    heal = 500,
                    source
                )
            )
        }
        this.finish()
    }
}

open class UpgradeToolTask(
    stepSize: Int,
    source: Being,
) : RWTask(stepSize, source, null) {
    override fun run() {
        if ((source as Being).cost(200)) {
            source.sendMsg(
                UpgradeToolEvent(
                    System.currentTimeMillis(),
                    "UpgradeTool",
                    source.topic,
                    30,
                    source
                )
            )
        }

        this.finish()
    }
}

// 养鱼所需

open class ATKTask(
    stepSize: Int,
    source: Being,
    target: Being
) : RWTask(stepSize, source, target) {

    override fun run() {
        source.sendMsg(
            ATKEvent(
                System.currentTimeMillis(),
                "ATK",
                (source as Being).atk,
                source.topic,
                source,
                target!!
            )
        )
        this.finish()
    }

}

open class StayTask(
    stepSize: Int,
    source: Being,
) : RWTask(stepSize, source, null) {
    override fun run() {
        source.sendMsg(
            StayEvent(
                System.currentTimeMillis(),
                "Stay",
                source.topic,
                source,
            )
        )
        this.finish()
    }
}

open class EarnTask(
    stepSize: Int,
    source: Being,
    private val earnSpeed: Int,
) : RWTask(stepSize, source, null) {
    override fun run() {
        source.sendMsg(
            EarnEvent(
                System.currentTimeMillis(),
                "Earn",
                earnSpeed + (Math.random() * 100).toInt(),
                source.topic,
                source,
            )
        )
        this.finish()
    }
}

open class GrowthTask(
    source: Being,
    val growthType: String,
    val growthValue: Int
) : RWTask(1, source, null) {
    override fun run() {
        source.sendMsg(
            GrowthEvent(
                System.currentTimeMillis(),
                "Growth",
                source.topic,
                source,
                growthType,
                growthValue
            )
        )
    }
}