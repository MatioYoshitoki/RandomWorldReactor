package com.matio.random.domain.entity

abstract class RWTask(
    val stepSize: Int,
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

open class ATKTask(
    stepSize: Int,
    source: Human,
    target: Human
) : RWTask(stepSize, source, target) {

    override fun run() {
        source.sendMsg(
            ATKEvent(
                System.currentTimeMillis(),
                "ATK",
                true,
                (source as Human).atk,
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
    source: Human,
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
    source: Human,
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

open class UpgradeWeaponTask(
    stepSize: Int,
    source: Human,
) : RWTask(stepSize, source, null) {
    override fun run() {
        if ((source as Human).cost(300)) {
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
    source: Human,
) : RWTask(stepSize, source, null) {
    override fun run() {
        if ((source as Human).cost(300)) {
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
    source: Human,
) : RWTask(stepSize, source, null) {
    override fun run() {
        if ((source as Human).cost(200)) {
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