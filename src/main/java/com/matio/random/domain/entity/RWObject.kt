package com.matio.random.domain.entity

import com.matio.random.infra.JFunction
import com.matio.random.infra.constants.HumanStatus
import com.matio.random.infra.utils.SinksUtils
import org.slf4j.LoggerFactory
import reactor.core.publisher.Sinks
import java.util.*
import java.util.function.Consumer
import java.util.stream.Stream


/**
 * RandomWorld实体基础类
 * 状态的修改都必须通过task来进行。外界无法对每个实体对象的内部状态进行修改
 * */
abstract class RWObject(
    val id: Long,
    val name: String,
    val zoneId: Long,
    val sound: Sinks.Many<RWEvent>?,
    val taskChannel: Sinks.Many<RWTask>?,
) {

    private val log = LoggerFactory.getLogger(javaClass)
    val taskStack: Stack<RWTask> = Stack()

    val handler: Consumer<RWEvent> = Consumer { handlerMsg(it) }

    abstract fun handlerMsg(event: RWEvent)

    abstract fun destroy()

    abstract fun subscribe(topic: String)

    abstract fun unsubscribe(topic: String)

    fun sendMsg(event: RWEvent) {
        if (sound != null) {
            try {
                SinksUtils.tryEmit(sound, event)
            } catch (e: Exception) {
                log.error("send msg failed!", e)
            }
        }
    }

    fun pushTask(task: RWTask) {
        if (taskChannel != null) {
            try {
                SinksUtils.tryEmit(taskChannel, task)
            } catch (e: Exception) {
                log.error("push task failed!", e)
            }
        }
    }

}

open class Human(
    id: Long,
    name: String,
    zoneId: Long,
    var heal: Int = 800 + (Math.random() * 800).toInt(),
    open var atk: Int = 100 + (Math.random() * 200).toInt(),
    sound: Sinks.Many<RWEvent>?,
    taskChannel: Sinks.Many<RWTask>?,
    var earnSpeed: Int = 30,
    private val vision: JFunction<Long, Stream<Human>>,
    var money: Int = (Math.random() * 200).toInt(),
    private var status: HumanStatus = HumanStatus.ALIVE
) : RWObject(
    id, name, zoneId, sound, taskChannel
) {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun handlerMsg(event: RWEvent) {
        if (event.source != this) {
            if (event.target == this || event.target == null) {
//                if (event !is TimeEvent) {
//                    log.info("${this.name}(${this.id}) receive msg: ${event.msg}")
//                }
                when (event) {
                    is TimeEvent -> eventBack(event)
                    is HealEvent -> {
                        if (event.target == this) {
                            this.heal += event.heal
                            log.info("${this.name}(${this.id}) 获得治疗！当前生命值: ${this.heal}")
                            eventBack(event)
                        }
                    }
                    is UpgradeWeaponEvent -> {
                        if (event.target == this) {
                            this.atk += event.atk
                            log.info("${this.name} 武器得到升级！当前攻击力: ${this.atk}")
                            eventBack(event)
                        }
                    }
                    is UpgradeToolEvent -> {
                        if (event.target == this) {
                            this.earnSpeed += event.earnSpeed
                            log.info("${this.name} 工具得到升级！当前挖矿效率: ${this.earnSpeed}")
                            eventBack(event)
                        }
                    }
                    is EarnEvent -> {
                        if (event.target == this) {
                            this.money += event.amount
                            log.info("${this.name} 获得金币${event.amount}。当前金币：${this.money}")
                            eventBack(event)
                        }
                    }
                    is ATKEvent -> {
                        if (event.target == this) {
                            this.heal -= event.damage
                            log.info("${this.name} 受到【${event.source!!.name}】攻击, 生命值减少${event.damage}。剩余生命值: ${this.heal}")
                            if (heal <= 0) {
                                this.destroy()
                            } else {
                                eventBack(event)
                            }
                        }
                    }
                }
            }
        }
    }


    private fun eventBack(event: RWEvent) {
        val rate = Math.random()
        when (event) {
            is ATKEvent -> {
                if (rate < 0.05) {
                    pushTask(StayTask(1, this))
                } else if (rate < 0.2) {
                    pushTask(EarnTask(1, this, this.earnSpeed))
                } else {
                    pushTask(ATKTask(1, this, event.source as Human))
                }
            }
            is EarnEvent -> {
                if (this.money in 200..299) {
                    if (rate < 0.5) {
                        pushTask(UpgradeToolTask(1, this))
                    } else {
                        pushTask(EarnTask(1, this, this.earnSpeed))
                    }
                } else if (this.money >= 300) {
                    val healPressure: Float = if (this.heal < 800) 1.5f else 1f
                    if (rate < (0.2f * healPressure)) {
                        pushTask(HealTask(1, this))
                    } else if (rate < (0.6 * healPressure)) {
                        pushTask(UpgradeWeaponTask(1, this))
                    } else {
                        pushTask(EarnTask(1, this, this.earnSpeed))
                    }
                }
            }
            else -> {
                if (rate < 0.1) {
                    val target = findAllHumanSameZone().filter { it != this }.findAny()
                    if (target.isPresent) {
                        pushTask(ATKTask(1, this, target.get()))
                    }
                } else if (rate < 0.8) {
                    pushTask(EarnTask(1, this, this.earnSpeed))
                } else {
                    pushTask(StayTask(1, this))
                }
            }
        }
    }


    private fun findAllHumanSameZone(): Stream<Human> {
        return vision.apply(this.zoneId)
    }

    override fun destroy() {
        synchronized(this) {
            log.warn("角色【${this.name}】(${this.id})死亡!")
            status = HumanStatus.DEAD
            sendMsg(ObjectDestroyEvent(123, "Destroy", "world", this))
        }
    }

    open fun cost(amount: Int): Boolean {
        if (amount > money) {
            log.warn("${this.name}(${this.id}) 余额不足！")
            return false
        }
        log.info("${this.name}(${this.id}) 花费: $amount, 剩余: ${this.money}")
        this.money = money - amount
        return true
    }

    fun isAlive(): Boolean {
        return status == HumanStatus.ALIVE
    }

    override fun subscribe(topic: String) {

    }

    override fun unsubscribe(topic: String) {
        TODO("Not yet implemented")
    }

}