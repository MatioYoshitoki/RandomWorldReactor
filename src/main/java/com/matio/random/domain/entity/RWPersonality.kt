package com.matio.random.domain.entity

import cn.hutool.core.util.RandomUtil
import com.matio.random.domain.entity.obj.Being
import reactor.util.function.Tuple3
import reactor.util.function.Tuples
import java.util.*
import kotlin.reflect.KClass

open class RWPersonality(
    private val personality: Int
) {

    private var eventBehavior: Map<KClass<out RWEvent>, SortedMap<Int, KClass<out RWTask>>> = mapOf()

    init {
        val position = position(personality)
        var randomRate = RandomUtil.randomInt(randomRange)
        val personalityMap: MutableMap<KClass<out RWEvent>, SortedMap<Int, KClass<out RWTask>>> =
            when (position.t1) {
                0 -> {
                    mutableMapOf(
                        ATKEvent::class to sortedMapOf(
                            5500 + randomRate to ATKTask::class,
                            6000 + randomRate to StayTask::class,
                            10000 to EarnTask::class,
                        )
                    )
                }
                1 -> {
                    if (randomRate >= 500) {
                        randomRate = 500
                    }
                    mutableMapOf(

                        ATKEvent::class to sortedMapOf(
                            5500 + randomRate to ATKTask::class,
                            6000 to StayTask::class,
                            10000 to EarnTask::class,
                        )
                    )
                }
                2 -> mutableMapOf(
                    ATKEvent::class to sortedMapOf(
                        5500 - randomRate to ATKTask::class,
                        6000 - randomRate to StayTask::class,
                        10000 to EarnTask::class,
                    )
                )
                3 -> {
                    if (randomRate >= 500) {
                        randomRate = 500
                    }
                    mutableMapOf(
                        ATKEvent::class to sortedMapOf(
                            5500 to ATKTask::class,
                            6000 - randomRate to StayTask::class,
                            10000 to EarnTask::class,
                        )
                    )
                }
                4 -> mutableMapOf(
                    ATKEvent::class to sortedMapOf(
                        5500 - randomRate to ATKTask::class,
                        6000 to StayTask::class,
                        10000 to EarnTask::class,
                    )
                )
                else -> mutableMapOf(
                    ATKEvent::class to sortedMapOf(
                        5500 to ATKTask::class,
                        6000 + randomRate to StayTask::class,
                        10000 to EarnTask::class,
                    )
                )
            }
        personalityMap[EarnEvent::class] = when (position.t2) {
            0 -> sortedMapOf(
                800 + randomRate to ATKTask::class,
                2500 + randomRate to StayTask::class,
                10000 to EarnTask::class,
            )
            1 -> {
                if (randomRate >= 1700) {
                    randomRate = 1700
                }
                sortedMapOf(
                    800 + randomRate to ATKTask::class,
                    2500 to StayTask::class,
                    10000 to EarnTask::class,
                )
            }
            2 -> {
                if (randomRate >= 800) {
                    randomRate = 800
                }
                sortedMapOf(
                    800 - randomRate to ATKTask::class,
                    2500 - randomRate to StayTask::class,
                    10000 to EarnTask::class,
                )
            }
            3 -> {
                if (randomRate >= 1700) {
                    randomRate = 1700
                }

                sortedMapOf(
                    800 to ATKTask::class,
                    2500 - randomRate to StayTask::class,
                    10000 to EarnTask::class,
                )
            }
            4 -> {
                if (randomRate >= 800) {
                    randomRate = 800
                }
                sortedMapOf(
                    800 - randomRate to ATKTask::class,
                    2500 to StayTask::class,
                    10000 to EarnTask::class,
                )
            }
            else -> sortedMapOf(
                800 to ATKTask::class,
                2500 + randomRate to StayTask::class,
                10000 to EarnTask::class,
            )
        }
        personalityMap[TimeEvent::class] = when (position.t3) {
            0 -> sortedMapOf(
                500 + randomRate to ATKTask::class,
                1500 + randomRate to StayTask::class,
                10000 to EarnTask::class,
            )
            1 -> {
                if (randomRate >= 1000) {
                    randomRate = 1000
                }
                sortedMapOf(
                    500 + randomRate to ATKTask::class,
                    1500 to StayTask::class,
                    10000 to EarnTask::class,
                )
            }
            2 -> {
                if (randomRate >= 500) {
                    randomRate = 500
                }
                sortedMapOf(
                    500 - randomRate to ATKTask::class,
                    1500 - randomRate to StayTask::class,
                    10000 to EarnTask::class,
                )
            }
            3 -> {
                if (randomRate >= 1000) {
                    randomRate = 1000
                }

                sortedMapOf(
                    500 to ATKTask::class,
                    1500 - randomRate to StayTask::class,
                    10000 to EarnTask::class,
                )
            }
            4 -> {
                if (randomRate >= 500) {
                    randomRate = 500
                }
                sortedMapOf(
                    500 - randomRate to ATKTask::class,
                    1500 to StayTask::class,
                    10000 to EarnTask::class,
                )
            }
            else -> sortedMapOf(
                500 to ATKTask::class,
                1500 + randomRate to StayTask::class,
                10000 to EarnTask::class,
            )
        }
        this.eventBehavior = personalityMap
    }

    fun randomTask(event: RWEvent, obj: Being): RWTask? {
        val taskBehavior = eventBehavior[event::class]!!
        val rate: Int = RandomUtil.randomInt(10000)
        var klazz: KClass<out RWTask>? = null
        for (entry in taskBehavior) {
            if (rate <= entry.key) {
                klazz = entry.value
                break
            }
        }
        return when (klazz) {
            ATKTask::class -> {
                if (event is ATKEvent) {
                    ATKTask(obj, event.source!! as Being)
                } else {
                    val target = obj.findAllHumanSameZone().filter { it != obj }.findAny()
                    if (target.isPresent) {
                        ATKTask(obj, target.get())
                    } else {
                        null
                    }
                }
            }
            EarnTask::class -> EarnTask(obj, obj.tryEarn())
            else -> StayTask(obj)
        }
    }

    fun personalityName(): String {
        return "" + personality
    }

    /**
     * 5个字母为一个分区，一个分区对应一个事件
     * 每个分区的第一位代表事件A->ATK, E->Earn, T->Time
     * 后面2位一组，每组开头代表任务, 第二位代表是提高或降低, U->UP代表提高，D->Down代表降低
     * 如AAUEDEAUEDTAUED代表ATK事件下ATK任务偏好提高, Earn任务偏好下架。。。。。。
     * */
    enum class PersonalityType {
        AAUED_EAUED_TAUED, AAUSD_EAUED_TAUED, AEUAD_EAUED_TAUED, AEUSD_EAUED_TAUED, ASUAD_EAUED_TAUED, ASUED_EAUED_TAUED, AAUED_EAUSD_TAUED, AAUSD_EAUSD_TAUED, AEUAD_EAUSD_TAUED, AEUSD_EAUSD_TAUED, ASUAD_EAUSD_TAUED, ASUED_EAUSD_TAUED, AAUED_EEUAD_TAUED, AAUSD_EEUAD_TAUED, AEUAD_EEUAD_TAUED, AEUSD_EEUAD_TAUED, ASUAD_EEUAD_TAUED, ASUED_EEUAD_TAUED, AAUED_EEUSD_TAUED, AAUSD_EEUSD_TAUED, AEUAD_EEUSD_TAUED, AEUSD_EEUSD_TAUED, ASUAD_EEUSD_TAUED, ASUED_EEUSD_TAUED, AAUED_ESUAD_TAUED, AAUSD_ESUAD_TAUED, AEUAD_ESUAD_TAUED, AEUSD_ESUAD_TAUED, ASUAD_ESUAD_TAUED, ASUED_ESUAD_TAUED, AAUED_ESUED_TAUED, AAUSD_ESUED_TAUED, AEUAD_ESUED_TAUED, AEUSD_ESUED_TAUED, ASUAD_ESUED_TAUED, ASUED_ESUED_TAUED, AAUED_EAUED_TAUSD, AAUSD_EAUED_TAUSD, AEUAD_EAUED_TAUSD, AEUSD_EAUED_TAUSD, ASUAD_EAUED_TAUSD, ASUED_EAUED_TAUSD, AAUED_EAUSD_TAUSD, AAUSD_EAUSD_TAUSD, AEUAD_EAUSD_TAUSD, AEUSD_EAUSD_TAUSD, ASUAD_EAUSD_TAUSD, ASUED_EAUSD_TAUSD, AAUED_EEUAD_TAUSD, AAUSD_EEUAD_TAUSD, AEUAD_EEUAD_TAUSD, AEUSD_EEUAD_TAUSD, ASUAD_EEUAD_TAUSD, ASUED_EEUAD_TAUSD, AAUED_EEUSD_TAUSD, AAUSD_EEUSD_TAUSD, AEUAD_EEUSD_TAUSD, AEUSD_EEUSD_TAUSD, ASUAD_EEUSD_TAUSD, ASUED_EEUSD_TAUSD, AAUED_ESUAD_TAUSD, AAUSD_ESUAD_TAUSD, AEUAD_ESUAD_TAUSD, AEUSD_ESUAD_TAUSD, ASUAD_ESUAD_TAUSD, ASUED_ESUAD_TAUSD, AAUED_ESUED_TAUSD, AAUSD_ESUED_TAUSD, AEUAD_ESUED_TAUSD, AEUSD_ESUED_TAUSD, ASUAD_ESUED_TAUSD, ASUED_ESUED_TAUSD, AAUED_EAUED_TEUAD, AAUSD_EAUED_TEUAD, AEUAD_EAUED_TEUAD, AEUSD_EAUED_TEUAD, ASUAD_EAUED_TEUAD, ASUED_EAUED_TEUAD, AAUED_EAUSD_TEUAD, AAUSD_EAUSD_TEUAD, AEUAD_EAUSD_TEUAD, AEUSD_EAUSD_TEUAD, ASUAD_EAUSD_TEUAD, ASUED_EAUSD_TEUAD, AAUED_EEUAD_TEUAD, AAUSD_EEUAD_TEUAD, AEUAD_EEUAD_TEUAD, AEUSD_EEUAD_TEUAD, ASUAD_EEUAD_TEUAD, ASUED_EEUAD_TEUAD, AAUED_EEUSD_TEUAD, AAUSD_EEUSD_TEUAD, AEUAD_EEUSD_TEUAD, AEUSD_EEUSD_TEUAD, ASUAD_EEUSD_TEUAD, ASUED_EEUSD_TEUAD, AAUED_ESUAD_TEUAD, AAUSD_ESUAD_TEUAD, AEUAD_ESUAD_TEUAD, AEUSD_ESUAD_TEUAD, ASUAD_ESUAD_TEUAD, ASUED_ESUAD_TEUAD, AAUED_ESUED_TEUAD, AAUSD_ESUED_TEUAD, AEUAD_ESUED_TEUAD, AEUSD_ESUED_TEUAD, ASUAD_ESUED_TEUAD, ASUED_ESUED_TEUAD, AAUED_EAUED_TEUSD, AAUSD_EAUED_TEUSD, AEUAD_EAUED_TEUSD, AEUSD_EAUED_TEUSD, ASUAD_EAUED_TEUSD, ASUED_EAUED_TEUSD, AAUED_EAUSD_TEUSD, AAUSD_EAUSD_TEUSD, AEUAD_EAUSD_TEUSD, AEUSD_EAUSD_TEUSD, ASUAD_EAUSD_TEUSD, ASUED_EAUSD_TEUSD, AAUED_EEUAD_TEUSD, AAUSD_EEUAD_TEUSD, AEUAD_EEUAD_TEUSD, AEUSD_EEUAD_TEUSD, ASUAD_EEUAD_TEUSD, ASUED_EEUAD_TEUSD, AAUED_EEUSD_TEUSD, AAUSD_EEUSD_TEUSD, AEUAD_EEUSD_TEUSD, AEUSD_EEUSD_TEUSD, ASUAD_EEUSD_TEUSD, ASUED_EEUSD_TEUSD, AAUED_ESUAD_TEUSD, AAUSD_ESUAD_TEUSD, AEUAD_ESUAD_TEUSD, AEUSD_ESUAD_TEUSD, ASUAD_ESUAD_TEUSD, ASUED_ESUAD_TEUSD, AAUED_ESUED_TEUSD, AAUSD_ESUED_TEUSD, AEUAD_ESUED_TEUSD, AEUSD_ESUED_TEUSD, ASUAD_ESUED_TEUSD, ASUED_ESUED_TEUSD, AAUED_EAUED_TSUAD, AAUSD_EAUED_TSUAD, AEUAD_EAUED_TSUAD, AEUSD_EAUED_TSUAD, ASUAD_EAUED_TSUAD, ASUED_EAUED_TSUAD, AAUED_EAUSD_TSUAD, AAUSD_EAUSD_TSUAD, AEUAD_EAUSD_TSUAD, AEUSD_EAUSD_TSUAD, ASUAD_EAUSD_TSUAD, ASUED_EAUSD_TSUAD, AAUED_EEUAD_TSUAD, AAUSD_EEUAD_TSUAD, AEUAD_EEUAD_TSUAD, AEUSD_EEUAD_TSUAD, ASUAD_EEUAD_TSUAD, ASUED_EEUAD_TSUAD, AAUED_EEUSD_TSUAD, AAUSD_EEUSD_TSUAD, AEUAD_EEUSD_TSUAD, AEUSD_EEUSD_TSUAD, ASUAD_EEUSD_TSUAD, ASUED_EEUSD_TSUAD, AAUED_ESUAD_TSUAD, AAUSD_ESUAD_TSUAD, AEUAD_ESUAD_TSUAD, AEUSD_ESUAD_TSUAD, ASUAD_ESUAD_TSUAD, ASUED_ESUAD_TSUAD, AAUED_ESUED_TSUAD, AAUSD_ESUED_TSUAD, AEUAD_ESUED_TSUAD, AEUSD_ESUED_TSUAD, ASUAD_ESUED_TSUAD, ASUED_ESUED_TSUAD, AAUED_EAUED_TSUED, AAUSD_EAUED_TSUED, AEUAD_EAUED_TSUED, AEUSD_EAUED_TSUED, ASUAD_EAUED_TSUED, ASUED_EAUED_TSUED, AAUED_EAUSD_TSUED, AAUSD_EAUSD_TSUED, AEUAD_EAUSD_TSUED, AEUSD_EAUSD_TSUED, ASUAD_EAUSD_TSUED, ASUED_EAUSD_TSUED, AAUED_EEUAD_TSUED, AAUSD_EEUAD_TSUED, AEUAD_EEUAD_TSUED, AEUSD_EEUAD_TSUED, ASUAD_EEUAD_TSUED, ASUED_EEUAD_TSUED, AAUED_EEUSD_TSUED, AAUSD_EEUSD_TSUED, AEUAD_EEUSD_TSUED, AEUSD_EEUSD_TSUED, ASUAD_EEUSD_TSUED, ASUED_EEUSD_TSUED, AAUED_ESUAD_TSUED, AAUSD_ESUAD_TSUED, AEUAD_ESUAD_TSUED, AEUSD_ESUAD_TSUED, ASUAD_ESUAD_TSUED, ASUED_ESUAD_TSUED, AAUED_ESUED_TSUED, AAUSD_ESUED_TSUED, AEUAD_ESUED_TSUED, AEUSD_ESUED_TSUED, ASUAD_ESUED_TSUED, ASUED_ESUED_TSUED,
    }

    companion object {
        private const val randomRange = 1500
//        private val base = mapOf(
//            ATKEvent::class to sortedMapOf(
//                5500 to ATKTask::class,
//                6000 to StayTask::class,
//                10000 to EarnTask::class,
//            ), EarnEvent::class to sortedMapOf(
//                800 to ATKTask::class,
//                2500 to StayTask::class,
//                10000 to EarnTask::class,
//            ), TimeEvent::class to sortedMapOf(
//                500 to ATKTask::class,
//                1500 to StayTask::class,
//                10000 to EarnTask::class,
//            )
//        )

        fun random(personalityType: Int): RWPersonality {
            //1 AUED 1+2+
            //2 AUSD 1+
            //3 EUAD 1-2-
            //4 EUSD 2-
            //5 SUAD 1-
            //6 SUED 2+
            val tmp = if (personalityType > 196) {
                196
            } else {
                personalityType
            }
            return RWPersonality(tmp)
        }

        private fun position(personality: Int): Tuple3<Int, Int, Int> {
            val t1: Int = personality / 36
            val m1: Int = personality % 36
            val t2: Int = m1 / 6
            val t3: Int = m1 % 6
            return Tuples.of(t1, t2, t3)
        }
    }

}