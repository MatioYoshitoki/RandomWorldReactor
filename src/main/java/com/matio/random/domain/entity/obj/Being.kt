package com.matio.random.domain.entity.obj

import com.matio.random.domain.entity.*
import com.matio.random.infra.JFunction
import com.matio.random.infra.config.TaskProperties
import com.matio.random.infra.constants.BeingStatus
import org.slf4j.LoggerFactory
import reactor.core.publisher.Sinks
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Stream
import kotlin.reflect.KClass


open class Being(
    id: Long,
    name: String,
    var heal: Int = 800 + (Math.random() * 800).toInt(),
    open var atk: Int = 100 + (Math.random() * 200).toInt(),
    taskProperties: TaskProperties,
    sound: Sinks.Many<RWEvent>?,
    taskChannel: Sinks.Many<RWTask>?,
    var earnSpeed: Int = 30,
    var money: Int = (Math.random() * 200).toInt(),
    private var status: BeingStatus = BeingStatus.ALIVE
) : RWObject(
    id, name, taskProperties, sound, taskChannel
) {

    private val log = LoggerFactory.getLogger(javaClass)
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
                        if (event.target == this && (event.source as Being).isAlive()) {
                            this.heal -= event.atk
                            log.info("${this.name} 受到【${event.source.name}】攻击, 生命值减少${event.atk}。剩余生命值: ${this.heal}")
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


    open fun eventBack(event: RWEvent) {
        var task = nextTask(event)
        for (i in 0..10) {
            if (task == null) {
                break
            }
            if (taskCountDownMap.containsKey(task::class)) {
                if ((taskCountDownMap[task::class]!! + (taskProperties.countDown[task::class.simpleName]
                        ?: 0)) < System.currentTimeMillis()
                ) {
                    break
                } else {
                    task = nextTask(event)
                }
            }
        }
        if (task != null) {
            taskCountDownMap[task::class] = System.currentTimeMillis()
            pushTask(task)
        }
    }

    open fun nextTask(event: RWEvent): RWTask? {
        val rate = Math.random()
        return when (event) {
            is ATKEvent -> {
                if (rate < 0.05) {
                    StayTask(1, this)
                } else if (rate < 0.5) {
                    EarnTask(1, this, this.earnSpeed)
                } else {
                    ATKTask(1, this, event.source as Being)
                }
            }
            is EarnEvent -> {
                if (this.money in 200..299) {
                    if (rate < 0.5) {
                        UpgradeToolTask(1, this)
                    } else {
                        EarnTask(1, this, this.earnSpeed)
                    }
                } else if (this.money >= 300) {
                    val healPressure: Float = if (this.heal < 800) 1.5f else 1f
                    if (rate < (0.2f * healPressure)) {
                        HealTask(1, this)
                    } else if (rate < (0.6 * healPressure)) {
                        UpgradeWeaponTask(1, this)
                    } else {
                        EarnTask(1, this, this.earnSpeed)
                    }
                } else {
                    null
                }
            }
            else -> {
                if (rate < 0.05) {
                    val target = findAllHumanSameZone().filter { it != this }.findAny()
                    if (target.isPresent) {
                        ATKTask(1, this, target.get())
                    } else {
                        null
                    }
                } else if (rate < 0.8) {
                    EarnTask(1, this, this.earnSpeed)
                } else {
                    StayTask(1, this)
                }
            }
        }
    }

    private fun findAllHumanSameZone(): Stream<Being> {
        return if (vision != null) {
            vision!!.apply(this.topic)
        } else {
            Stream.empty()
        }
    }

    override fun destroy() {
        synchronized(this) {
            log.warn("角色【${this.name}】(${this.id})死亡!")
            status = BeingStatus.DEAD
            sendMsg(ObjectDestroyEvent(123, "Destroy", topic, this))
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
        return status == BeingStatus.ALIVE
    }

    fun openEyes(vision: JFunction<String, Stream<Being>>) {
        this.vision = vision
    }

    override fun subscribe(topic: String) {

    }

    override fun unsubscribe(topic: String) {
        TODO("Not yet implemented")
    }

}