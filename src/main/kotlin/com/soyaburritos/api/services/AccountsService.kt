package com.soyaburritos.api.services

import com.soyaburritos.api.converters.mapToAccountsEntity
import com.soyaburritos.api.db.Accounts
import com.soyaburritos.api.db.AccountsToUsers
import com.soyaburritos.api.db.Users
import com.soyaburritos.api.entities.AccountEntity
import com.soyaburritos.api.exceptions.AccountsException
import com.soyaburritos.api.exceptions.UsersException
import com.soyaburritos.api.validators.validateCurCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal

class AccountsService {
    private val log: Logger = LoggerFactory.getLogger(AccountsService::class.java)

    fun getAllAccounts(): List<AccountEntity> {
        return transaction {
            Accounts.selectAll()
                .map { mapToAccountsEntity(it) }
        }
    }

    fun getAccount(accountId: Int): AccountEntity? {
        return transaction {
            Accounts.select { Accounts.accountId eq accountId }
                .map { mapToAccountsEntity(it) }
                .firstOrNull()
        }
    }

    fun createAccount(accountToCreate: AccountEntity): Int {
        validateCurCode(accountToCreate.curCode)
        return transaction {
            Accounts.insert {
                it[amount] = accountToCreate.amount
                it[curCode] = accountToCreate.curCode
            } get Accounts.accountId
        }
    }

    fun updateAccountBalance(accountId: Int, depositAmount: BigDecimal): AccountEntity? {
        var currentAccount = transaction {
            Accounts.select { Accounts.accountId eq accountId }.forUpdate()
                .map { mapToAccountsEntity(it) }
                .firstOrNull()
        }

        if (currentAccount != null) {
            val newAmount: BigDecimal = currentAccount.amount.add(depositAmount)

            if (newAmount < BigDecimal.ZERO) {
                throw AccountsException("Zero amount on account %s".format(accountId))
            }

            transaction {
                Accounts.update({ Accounts.accountId eq accountId }) {
                    it[amount] = newAmount
                }
            }
            currentAccount = AccountEntity(accountId, newAmount, currentAccount.curCode)
        }

        return currentAccount
    }

    fun deleteAccount(accountId: Int): Int {
        return transaction {
            Accounts.deleteWhere { Accounts.accountId eq accountId }
        }
    }

    fun addAccountToUser(accountId: Int, userId: Int) {
        transaction {
            val accountExists =
                Accounts.select {
                    Accounts.accountId eq accountId
                }.count() > 0

            val userExists =
                Users.select {
                    Users.userId eq userId
                }.count() > 0

            if (!accountExists) {
                throw AccountsException("Account with id %s not exists".format(accountId))
            }

            if (!userExists) {
                throw UsersException("User with id %s not exists".format(userExists))
            }

            try {
                AccountsToUsers.insert {
                    it[AccountsToUsers.userId] = userId
                    it[AccountsToUsers.accountId] = accountId
                }
            } catch (ex: ExposedSQLException) {
                if (ex.message?.contains("Unique index or primary key violation")!!) {
                    throw AccountsException("Try to assign already assigned account")
                } else {
                    throw ex
                }
            }
        }
    }
}
