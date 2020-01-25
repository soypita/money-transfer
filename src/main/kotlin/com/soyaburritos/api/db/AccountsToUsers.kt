package com.soyaburritos.api.db

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object AccountsToUsers : Table("accounts_to_users") {
    val userId = integer("user_id").references(Users.userId, onDelete = ReferenceOption.CASCADE).primaryKey()
    val accountId =
        integer("account_id").references(Accounts.accountId, onDelete = ReferenceOption.CASCADE).primaryKey()
}