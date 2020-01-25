package com.soyaburritos.api.entities

import java.math.BigDecimal

data class MoneyTransaction(
    val amount: BigDecimal,
    val curCode: String,
    val fromAccountId: Int,
    val toAccountId: Int
)