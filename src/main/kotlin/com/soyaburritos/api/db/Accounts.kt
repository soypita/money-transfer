package com.soyaburritos.api.db

import org.jetbrains.exposed.sql.Table

object Accounts : Table("accounts") {
    val accountId = integer("id").autoIncrement().primaryKey()
    val amount = decimal("amount",19, 4)
    val curCode = varchar("cur_code", 30)
}