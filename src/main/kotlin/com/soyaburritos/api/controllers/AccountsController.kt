package com.soyaburritos.api.controllers

import com.soyaburritos.api.entities.AccountEntity
import com.soyaburritos.api.exceptions.AccountsException
import com.soyaburritos.api.services.AccountsService
import com.soyaburritos.api.validators.validateAccountId
import com.soyaburritos.api.validators.validateAmount
import com.soyaburritos.api.validators.validateUserId
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import java.math.BigDecimal

internal fun Routing.apiAccounts(accountsService: AccountsService) {
    route("/accounts") {
        get("/all") {
            call.respond(accountsService.getAllAccounts())
        }

        get("/{accountId}") {
            val accountId: Int? = call.parameters["accountId"]?.toIntOrNull()

            validateAccountId(accountId)

            val account = accountId?.let { it1 -> accountsService.getAccount(it1) }

            if (account != null) {
                call.respond(account)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        get("/{accountId}/balance") {
            val accountId: Int? = call.parameters["accountId"]?.toIntOrNull()

            validateAccountId(accountId)

            val account = accountId?.let { it1 -> accountsService.getAccount(it1) }

            account?.let { call.respond(account.amount) } ?: call.respond(HttpStatusCode.NotFound)
        }

        post("/create") {
            val accountToCreate = call.receive<AccountEntity>()
            validateAmount(accountToCreate.amount.toPlainString())
            call.respond(accountsService.createAccount(accountToCreate))
        }

        post("/{accountId}/deposit/{amount}") {
            val accountId: Int? = call.parameters["accountId"]?.toIntOrNull()

            validateAccountId(accountId)
            validateAmount(call.parameters["amount"])

            val depositAmount = BigDecimal(call.parameters["amount"])

            accountId?.let {
                val updatedAccount = accountsService.updateAccountBalance(accountId, depositAmount)
                updatedAccount?.let { it1 -> call.respond(it1) } ?: call.respond(HttpStatusCode.NotFound)
            }
        }

        post("/{accountId}/withdraw/{amount}") {
            val accountId: Int? = call.parameters["accountId"]?.toIntOrNull()

            validateAccountId(accountId)
            validateAmount(call.parameters["amount"])

            val depositAmount = BigDecimal(call.parameters["amount"])

            accountId?.let {
                val updatedAccount = accountsService.updateAccountBalance(accountId, depositAmount.negate())
                updatedAccount?.let { it1 -> call.respond(it1) } ?: call.respond(HttpStatusCode.NotFound)
            }
        }

        post("/{accountId}/addToUser/{userId}") {
            val accountId: Int? = call.parameters["accountId"]?.toIntOrNull()
            val userId: Int? = call.parameters["userId"]?.toIntOrNull()

            validateUserId(userId)
            validateAccountId(accountId)

            accountsService.addAccountToUser(accountId!!, userId!!)

            call.respond(HttpStatusCode.OK)
        }

        delete("/{accountId}") {
            val accountId: Int? = call.parameters["accountId"]?.toIntOrNull()

            validateAccountId(accountId)

            accountId?.let {
                val deleteCount = accountsService.deleteAccount(accountId)
                if (deleteCount == 1) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}