package com.soyaburritos.api.converters

import com.soyaburritos.api.db.Accounts
import com.soyaburritos.api.db.Users
import com.soyaburritos.api.entities.AccountEntity
import com.soyaburritos.api.entities.UserWithAccountInfoEntity
import org.jetbrains.exposed.sql.ResultRow

fun mapToUserEntity(it: ResultRow) = UserWithAccountInfoEntity(
    userId = it[Users.userId],
    firstName = it[Users.firstName],
    lastName = it[Users.lastName],
    accounts = emptyList()
)

fun mapToAccountsEntity(it: ResultRow) = AccountEntity(
    accountId = it[Accounts.accountId],
    amount = it[Accounts.amount],
    curCode = it[Accounts.curCode]
)
