package com.soyaburritos.api.module

import com.soyaburritos.api.controllers.api
import com.soyaburritos.api.db.DatabaseFactory
import com.soyaburritos.api.services.AccountsService
import com.soyaburritos.api.services.TransferService
import com.soyaburritos.api.services.UsersService
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.jackson.jackson
import io.ktor.routing.*

fun Application.module() {
    val accountService = AccountsService()
    val usersService = UsersService()
    val transferService = TransferService()

    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        jackson {}
    }

    DatabaseFactory.init()

    install(Routing) {
        api(accountService, usersService, transferService)
    }
}