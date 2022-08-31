package com.rw.websocket.domain.dto.request

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.rw.random.common.constants.BeingStatus

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
class FishDetails(
    var id: String,
    var name: String,
    var hasMaster: Boolean,
    var masterId: Long?,
    var weight: Int,
    var maxHeal: Int,
    var heal: Int,
    var recoverSpeed: Int,
    var atk: Int,
    var def: Int,
    var earnSpeed: Int,
    val dodge: Int,
    var money: Int,
    var status: BeingStatus,
    val personality: Int,
    val personalityName: String,
) {
    companion object {
        val KEYS =
            listOf(
                "id",
                "name",
                "weight",
                "maxHeal",
                "heal",
                "hasMaster",
                "masterId",
                "recoverSpeed",
                "atk",
                "def",
                "earnSpeed",
                "dodge",
                "money",
                "status",
                "personalityId",
                "personalityRandomRate",
            )
    }
}
