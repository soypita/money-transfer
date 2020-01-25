package com.soyaburritos.api.entities

import java.math.BigDecimal

data class AccountEntity(
    val accountId: Int?,
    val amount: BigDecimal,
    val curCode: String
)