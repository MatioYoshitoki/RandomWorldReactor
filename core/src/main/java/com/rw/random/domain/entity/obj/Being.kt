package com.rw.random.domain.entity.obj

import cn.hutool.core.util.RandomUtil
import com.rw.random.domain.entity.*
import com.rw.random.infra.JFunction
import com.rw.random.infra.config.TaskProperties
import com.rw.random.common.constants.BeingStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Sinks
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Stream
import kotlin.reflect.KClass


open class Being(
    id: Long,
    name: String,
    hasMaster: Boolean = false,
    var heal: Int = 800 + (Math.random() * 800).toInt(),
    open var atk: Int = 100 + (Math.random() * 200).toInt(),
    taskProperties: TaskProperties,
    sound: Sinks.Many<RWEvent>?,
    taskChannel: Sinks.Many<RWTask>?,
    var earnSpeed: Int = 30,
    var money: Long = (Math.random() * 200).toLong(),
    var status: BeingStatus = BeingStatus.ALIVE
) : RWObject(
    id, name, hasMaster, taskProperties, sound, taskChannel
) {

    val log: Logger = LoggerFactory.getLogger(javaClass)
    var vision: JFunction<String, Stream<Being>>? = null

    private val taskCountDownMap = ConcurrentHashMap<KClass<out RWTask>, Long>()

    override fun handlerMsg(event: RWEvent) {
        if (event.source != this) {
            if (event.target == this || event.target == null) {
                when (event) {
                    is TimeEvent -> eventBack(event)
                    is HealEvent -> {
                        if (event.target == this) {
                            this.heal += event.heal
                            log.debug("${this.name}(${this.id}) 获得治疗！当前生命值: ${this.heal}")
                            eventBack(event)
                        }
                    }
                    is UpgradeWeaponEvent -> {
                        if (event.target == this) {
                            this.atk += event.atk
                            log.debug("${this.name} 武器得到升级！当前攻击力: ${this.atk}")
                            eventBack(event)
                        }
                    }
                    is UpgradeToolEvent -> {
                        if (event.target == this) {
                            this.earnSpeed += event.earnSpeed
                            log.debug("${this.name} 工具得到升级！当前挖矿效率: ${this.earnSpeed}")
                            eventBack(event)
                        }
                    }
                    is EarnEvent -> {
                        if (event.target == this) {
                            this.money += event.amount
                            log.debug("${this.name} 获得金币${event.amount}。当前金币：${this.money}")
                            eventBack(event)
                        }
                    }
                    is ATKEvent -> {
                        if (event.target == this && (event.source as Being).isAlive()) {
                            this.heal -= event.atk
                            log.debug("${this.name} 受到【${event.source.name}】攻击, 生命值减少${event.atk}。剩余生命值: ${this.heal}")
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


    open fun eventBack(event: RWEvent) {
        val currentTime = System.currentTimeMillis()
        val excludeTask = taskCountDownMap.filter {
            it.value + (taskProperties.countDown[it.key.simpleName] ?: 0) < currentTime
        }.keys
        val task = nextTask(event, excludeTask)
        if (task != null) {
            taskCountDownMap[task::class] = System.currentTimeMillis()
            pushTask(task)
        }
    }

    open fun nextTask(event: RWEvent, excludeTask: Set<KClass<out RWTask>>): RWTask? {
        val rate = Math.random()
        return when (event) {
            is ATKEvent -> {
                if (rate < 0.05) {
                    StayTask(this)
                } else if (rate < 0.5) {
                    EarnTask(this, this.earnSpeed)
                } else {
                    ATKTask(this, event.source as Being)
                }
            }
            is EarnEvent -> {
                if (this.money in 200..299) {
                    if (rate < 0.5) {
                        UpgradeToolTask(1, this)
                    } else {
                        EarnTask(this, this.earnSpeed)
                    }
                } else if (this.money >= 300) {
                    val healPressure: Float = if (this.heal < 800) 1.5f else 1f
                    if (rate < (0.2f * healPressure)) {
                        HealTask(1, this)
                    } else if (rate < (0.6 * healPressure)) {
                        UpgradeWeaponTask(1, this)
                    } else {
                        EarnTask(this, this.earnSpeed)
                    }
                } else {
                    null
                }
            }
            else -> {
                if (rate < 0.05) {
                    val target = findAllHumanSameZone().filter { it != this }.findAny()
                    if (target.isPresent) {
                        ATKTask(this, target.get())
                    } else {
                        null
                    }
                } else if (rate < 0.8) {
                    EarnTask(this, this.earnSpeed)
                } else {
                    StayTask(this)
                }
            }
        }
    }

    fun findAllHumanSameZone(): Stream<Being> {
        return if (vision != null) {
            vision!!.apply(this.topic)
        } else {
            Stream.empty()
        }
    }

    override fun destroy(target: RWObject?) {
        synchronized(this) {
            log.warn("角色【${this.name}】死亡!")
            status = BeingStatus.DEAD
            sendMsg(ObjectDestroyEvent(123, "Destroy", topic, this, target))
        }
    }

    open fun cost(amount: Int): Boolean {
        if (amount > money) {
            log.warn("${this.name}(${this.id}) 余额不足！")
            return false
        }
        log.debug("${this.name}(${this.id}) 花费: $amount, 剩余: ${this.money}")
        this.money = money - amount
        return true
    }

    fun isAlive(): Boolean {
        return status == BeingStatus.ALIVE
    }

    fun isSleep(): Boolean {
        return status == BeingStatus.SLEEP
    }

    fun isDead(): Boolean {
        return status == BeingStatus.DEAD
    }

    fun openEyes(vision: JFunction<String, Stream<Being>>) {
        this.vision = vision
    }

    fun tryEarn(): Int {
        return earnSpeed + RandomUtil.randomInt(200)
    }

    override fun subscribe(topic: String) {

    }

    override fun unsubscribe(topic: String) {
        TODO("Not yet implemented")
    }

}
