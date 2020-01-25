package com.soyaburritos.api.entities

data class UserWithAccountInfoEntity (
    val userId: Int?,
    val firstName: String,
    val lastName: String,
    val accounts: List<AccountEntity>
)