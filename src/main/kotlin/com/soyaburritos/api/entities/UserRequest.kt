package com.soyaburritos.api.entities

data class UserRequest(
    val firstName: String,
    val lastName: String,
    val accountIds: List<Int>?
)