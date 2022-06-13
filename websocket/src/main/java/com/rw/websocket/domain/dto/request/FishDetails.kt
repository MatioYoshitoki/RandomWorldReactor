package com.rw.websocket.domain.dto.request

import com.rw.random.common.constants.BeingStatus

class FishDetails(
    var id: Long,
    var name: String,
    var hasMaster: Boolean,
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
) {
    companion object {
        val KEYS =
            listOf(
                "id",
                "name",
                "weight",
                "maxHeal",
                "heal",
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
