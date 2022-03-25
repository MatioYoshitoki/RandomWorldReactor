package com.matio.random.domain.entity.obj

import com.matio.random.domain.entity.*
import com.matio.random.infra.config.TaskProperties
import com.matio.random.infra.constants.BeingStatus
import reactor.core.publisher.Sinks
import java.util.stream.Stream

open class Fish(
    id: Long,
    name: String,
    heal: Int = 800 + (Math.random() * 800).toInt(),
    atk: Int = 100 + (Math.random() * 200).toInt(),
    taskProperties: TaskProperties,
    sound: Sinks.Many<RWEvent>?,
    taskChannel: Sinks.Many<RWTask>?,
    earnSpeed: Int = 30,
    money: Int = (Math.random() * 200).toInt(),
    status: BeingStatus = BeingStatus.ALIVE,
    private val maxHeal: Int,
    private val recover: Int,
    private val dodge: Int, // max 100
    private val def: Int,
    private val personality: RWPersonality,
) : Being(id, name, heal, atk, taskProperties, sound, taskChannel, earnSpeed, money, status) {

    override fun nextTask(event: RWEvent): RWTask? {
        val rate = Math.random()
        return when (event) {
            is ATKEvent -> {
//                personality.randomTask(event)
                null
            }
            is EarnEvent -> {
                if (this.money >= 300) {
                    GrowthTask(this, "", 0)
                } else {
                    null
                }
            }
            else -> {
                personality.randomTask(event)
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

    fun title(): String {
        return ""
    }

    fun score(): Int {
        return 0
    }

}