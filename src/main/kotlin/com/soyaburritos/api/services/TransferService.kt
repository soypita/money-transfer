package com.soyaburritos.api.services

import com.soyaburritos.api.converters.mapToAccountsEntity
import com.soyaburritos.api.db.Accounts
import com.soyaburritos.api.entities.AccountEntity
import com.soyaburritos.api.entities.MoneyTransaction
import com.soyaburritos.api.exceptions.AccountsException
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.math.BigDecimal

class TransferService {

    fun transferMoney(transfer: MoneyTransaction) {
        transaction {
            val accountFrom: AccountEntity =
                Accounts.select { Accounts.accountId eq transfer.fromAccountId }.forUpdate()
                    .map { mapToAccountsEntity(it) }
                    .firstOrNull()
                    ?: throw AccountsException("Account with id %s not exist".format(transfer.fromAccountId))


            val accountTo =
                Accounts.select { Accounts.accountId eq transfer.toAccountId }.forUpdate()
                    .map { mapToAccountsEntity(it) }
                    .firstOrNull()
                    ?: throw AccountsException("Account with id %s not exist".format(transfer.toAccountId))

            if (accountFrom.curCode != transfer.curCode) {
                throw AccountsException("Transaction currency different from source accounts")
            }

            if (accountFrom.curCode != accountTo.curCode) {
                throw AccountsException("Accounts with different currencies")
            }

            val leftSourceBalance = accountFrom.amount.subtract(transfer.amount)
            if (leftSourceBalance < BigDecimal.ZERO) {
                throw AccountsException("No enough money on source account")
            }

            Accounts.update({ Accounts.accountId eq transfer.fromAccountId }) {
                it[amount] = leftSourceBalance
            }

            val newDestBalance = accountTo.amount.add(transfer.amount)

            Accounts.update({ Accounts.accountId eq transfer.toAccountId }) {
                it[amount] = newDestBalance
            }
        }
    }

}