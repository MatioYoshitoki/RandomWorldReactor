package com.rw.random.domain.entity

import cn.hutool.core.util.RandomUtil
import com.rw.random.domain.entity.obj.Being
import com.rw.random.domain.entity.obj.Fish
import com.rw.random.infra.constants.GrowthType
import reactor.util.function.Tuples

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
    source: Being,
    target: Being
) : RWTask(1, source, target) {

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
    source: Being,
) : RWTask(1, source, null) {
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
    source: Being,
    private val earnSpeed: Int,
) : RWTask(1, source, null) {
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
    source: Fish,
    private val growthType: GrowthType,
    private val growthValue: Int
) : RWTask(1, source, null) {
    override fun run() {
        if ((source as Fish).cost((source.weight * 0.5).toInt())) {
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

    companion object {
        private val growthTypeList =
            listOf(GrowthType.EARN_SPEED, GrowthType.MAX_HEAL, GrowthType.RECOVER_SPEED, GrowthType.DEF, GrowthType.ATK)
        private val growthValueMap = mapOf(
            GrowthType.EARN_SPEED to Tuples.of(20, 60),
            GrowthType.MAX_HEAL to Tuples.of(1000, 3000),
            GrowthType.RECOVER_SPEED to Tuples.of(10, 50),
            GrowthType.DEF to Tuples.of(80, 240),
            GrowthType.ATK to Tuples.of(120, 360)
        )

        fun randomType(): GrowthType {
            return growthTypeList.random()
        }

        fun randomValue(type: GrowthType): Int {
            val min = growthValueMap[type]!!.t1
            val max = growthValueMap[type]!!.t2
            return RandomUtil.randomInt(min, max)
        }
    }
}