package com.soyaburritos.api.controllers

import com.soyaburritos.api.entities.MoneyTransaction
import com.soyaburritos.api.services.TransferService
import com.soyaburritos.api.validators.validateAccountId
import com.soyaburritos.api.validators.validateAmount
import com.soyaburritos.api.validators.validateCurCode
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import io.ktor.routing.route

internal fun Routing.apiTransfer(transferService: TransferService) {
    route("/transfer") {
        post {
            val transferTransaction = call.receive<MoneyTransaction>()
            validateAccountId(transferTransaction.fromAccountId)
            validateAccountId(transferTransaction.toAccountId)
            validateCurCode(transferTransaction.curCode)
            validateAmount(transferTransaction.amount.toPlainString())

            call.respond(transferService.transferMoney(transferTransaction))
        }
    }
}