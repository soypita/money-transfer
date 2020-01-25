package com.soyaburritos.api.db

import org.jetbrains.exposed.sql.Table

object Users : Table("users") {
    val userId = integer("id").autoIncrement().primaryKey()
    val firstName = varchar("first_name", 256)
    val lastName = varchar("last_name", 256)
}