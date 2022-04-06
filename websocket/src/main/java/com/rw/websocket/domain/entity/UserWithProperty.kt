package com.rw.websocket.domain.entity

class UserWithProperty(
    val userId: Long,
    val userName: String,
    val accessToken: String,
    var exp: Long,
    var money: Long,
    var fishMaxCount: Long,
) {
    companion object {
        const val USER_ID_FIELD = "user_id"
        const val USER_NAME_FIELD = "user_name"
        const val ACCESS_TOKEN_FIELD = "access_token"
        const val EXP_FIELD = "exp"
        const val MONEY_FIELD = "money_field"
        const val FISH_MAX_COUNT_FIELD = "fish_max_count"
    }
}