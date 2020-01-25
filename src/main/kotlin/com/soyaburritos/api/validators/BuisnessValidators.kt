package com.soyaburritos.api.validators

import com.soyaburritos.api.exceptions.AccountsException
import com.soyaburritos.api.exceptions.AmountException
import com.soyaburritos.api.exceptions.CurrencyException
import com.soyaburritos.api.exceptions.UsersException
import java.math.BigDecimal
import java.util.*

fun validateUserId(userId: Int?) {
    if (userId == null || userId == 0) {
        throw UsersException("UserId required")
    }
}

fun validateAccountId(accountId: Int?) {
    if (accountId == null || accountId == 0) {
        throw AccountsException("AccountId required")
    }
}

fun validateCurCode(curCode: String) {
    try {
        Currency.getInstance(curCode)
    } catch (ex: Exception) {
        throw CurrencyException("Currency validation failed")
    }
}

fun validateAmount(validatedAmount: String?) {
    if (validatedAmount == null) {
        throw AmountException("Wrong amount value")
    }

    try {
        BigDecimal(validatedAmount)
    } catch (ex: Exception) {
        throw AmountException("Wrong amount value")
    }
}
