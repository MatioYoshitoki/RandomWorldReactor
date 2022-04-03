package com.rw.random.domain.entity.obj

import cn.hutool.core.util.RandomUtil
import com.rw.random.domain.entity.*
import com.rw.random.infra.config.TaskProperties
import com.rw.random.common.constants.BeingStatus
import com.rw.random.infra.constants.GrowthType
import com.rw.random.infra.utils.BattleUtils
import com.rw.random.infra.utils.SinksUtils
import reactor.core.publisher.Sinks

open class Fish(
    id: Long,
    name: String,
    hasMaster: Boolean = false,
    var weight: Int = 800, // 体重
    var maxHeal: Int = 10000 + (Math.random() * 5000).toInt(),
    heal: Int = maxHeal,
    var recoverSpeed: Int = 100,
    atk: Int = 300 + (Math.random() * 200).toInt(),
    var def: Int = 90 + (Math.random() * 180).toInt(),
    earnSpeed: Int = 30,
    private val dodge: Int = 5, // max 100
    money: Int = (Math.random() * 200).toInt(),
    taskProperties: TaskProperties,
    sound: Sinks.Many<RWEvent>?,
    taskChannel: Sinks.Many<RWTask>?,
    status: BeingStatus = BeingStatus.ALIVE,
    val personality: RWPersonality,
) : Being(id, name, hasMaster, heal, atk, taskProperties, sound, taskChannel, earnSpeed, money, status) {

    override fun handlerMsg(event: RWEvent) {
        if (event.source != this) {
            if (event.target == this || event.target == null) {
                if (this.money >= this.weight * 0.5) {
                    val growthType = GrowthTask.randomType()
                    growth(growthType, GrowthTask.randomValue(growthType))
                }
                when (event) {
                    is TimeEvent -> {
                        recover()
                        eventBack(event)
                    }
                    is GrowthEvent -> {
                        if (event.target == this) {
                            growth(event.growthType, event.growthValue)
                            eventBack(event)
                        }
                    }
                    is EarnEvent -> {
                        if (event.target == this) {
                            earn(event.amount)
                            eventBack(event)
                        }
                    }
                    is ATKEvent -> {
                        if (event.target == this && (event.source as Being).isAlive()) {
                            beAtk(event)
                            if (heal <= 0) {
                                this.destroy(event.source)
                            } else {
                                eventBack(event)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun nextTask(event: RWEvent): RWTask? {
        return when (event) {
            is EarnEvent -> null
            else -> personality.randomTask(event, this)
        }
    }

    private fun growth(growthType: GrowthType, growthValue: Int) {
        this.weight += 1.coerceAtLeast((this.weight * 0.05).toInt())
        when (growthType) {
            GrowthType.ATK -> atk += growthValue
            GrowthType.DEF -> def += growthValue
            GrowthType.MAX_HEAL -> maxHeal += growthValue
            GrowthType.RECOVER_SPEED -> recoverSpeed += growthValue
            GrowthType.EARN_SPEED -> earnSpeed += growthValue
        }
        val typeTag = when (growthType) {
            GrowthType.ATK -> "攻击力"
            GrowthType.DEF -> "防御力"
            GrowthType.MAX_HEAL -> "最大生命值"
            GrowthType.RECOVER_SPEED -> "生命恢复速度"
            GrowthType.EARN_SPEED -> "进食速度"
        }
        val currentValue = when (growthType) {
            GrowthType.ATK -> atk
            GrowthType.DEF -> def
            GrowthType.MAX_HEAL -> maxHeal
            GrowthType.RECOVER_SPEED -> recoverSpeed
            GrowthType.EARN_SPEED -> earnSpeed
        }
        log.info("${this.name} 长大了！当前体重: ${this.weight}克。与此同时，$typeTag 提升了 $growthValue, 当前$typeTag: $currentValue")
    }

    private fun recover() {
        if (heal + recoverSpeed >= maxHeal) heal = maxHeal else heal += recoverSpeed
        if (heal > maxHeal) heal = maxHeal
    }

    private fun earn(amount: Int) {
        this.money += amount
        log.info("${this.name} 吃到${amount}克鱼粮！。")
    }

    private fun beAtk(event: ATKEvent) {
        val rate = RandomUtil.randomInt(100)
        if (rate < this.dodge) {
            log.info("${this.name} 受到【${event.source!!.name}】攻击, 但未命中！")
            SinksUtils.tryEmit(
                this.sound!!,
                BeAtkEvent(System.currentTimeMillis(), "BeAtk", this.topic, this, event.source,
                    success = false,
                    ct = false,
                    damage = 0
                )
            )
        } else {
            val damage = (event.atk * (1 - BattleUtils.defRate(this.def))).toInt()
            this.heal -= damage
            SinksUtils.tryEmit(
                this.sound!!,
                BeAtkEvent(
                    System.currentTimeMillis(), "BeAtk", this.topic, this, event.source!!,
                    success = true,
                    ct = false,
                    damage = damage
                )
            )
            log.info("${this.name} 受到【${event.source.name}】攻击, 生命值减少${damage}。剩余生命值: ${this.heal}")
        }

    }

    fun title(): String {
        return ""
    }

    fun score(): Int {
        return 0
    }

}