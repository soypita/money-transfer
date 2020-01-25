package com.soyaburritos.api.validators

import com.soyaburritos.api.exceptions.AccountsException
import com.soyaburritos.api.exceptions.UsersException

fun validateUserId(userId: Int?) {
    if (userId == null || userId == 0) {
        throw UsersException("UserId not provided")
    }
}

fun validateAccountId(accountId: Int?) {
    if (accountId == null || accountId == 0) {
        throw AccountsException("AccountId not provided")
    }
}
