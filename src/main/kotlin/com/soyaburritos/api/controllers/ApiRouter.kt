package com.soyaburritos.api.controllers

import com.soyaburritos.api.services.AccountsService
import com.soyaburritos.api.services.TransferService
import com.soyaburritos.api.services.UsersService
import io.ktor.routing.*

internal fun Routing.api(
    accountsService: AccountsService,
    usersService: UsersService,
    transferService: TransferService
) {
    apiAccounts(accountsService)
    apiUsers(usersService)
    apiTransfer(transferService)
}

