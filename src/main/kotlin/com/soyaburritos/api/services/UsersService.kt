package com.soyaburritos.api.services

import com.soyaburritos.api.converters.mapToAccountsEntity
import com.soyaburritos.api.converters.mapToUserEntity
import com.soyaburritos.api.db.Accounts
import com.soyaburritos.api.db.AccountsToUsers
import com.soyaburritos.api.db.Users
import com.soyaburritos.api.entities.AccountEntity
import com.soyaburritos.api.entities.UserRequest
import com.soyaburritos.api.entities.UserWithAccountInfoEntity
import com.soyaburritos.api.exceptions.AccountsException
import com.soyaburritos.api.exceptions.UsersException
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.stream.Collectors.toList

class UsersService {

    fun getAllUsers(withAccountsInfo: Boolean?): List<UserWithAccountInfoEntity> {
        var response = transaction {
            Users.selectAll()
                .map { mapToUserEntity(it) }
        }

        if (withAccountsInfo != null && withAccountsInfo) {
            response = response.stream()
                .map {
                    UserWithAccountInfoEntity(
                        it.userId,
                        it.firstName,
                        it.lastName,
                        enrichAccountsInfo(it.userId!!)
                    )
                }
                .collect(toList())
        }

        return response
    }

    fun getUser(userId: Int, withAccountInfo: Boolean?): UserWithAccountInfoEntity? {
        var response = transaction {
            Users.select { Users.userId eq userId }
                .map { mapToUserEntity(it) }
                .firstOrNull()
        }

        response?.let {
            if (withAccountInfo != null && withAccountInfo) {
                response = UserWithAccountInfoEntity(
                    it.userId,
                    it.firstName,
                    it.lastName,
                    enrichAccountsInfo(it.userId!!)
                )
            }
        }

        return response
    }


    private fun enrichAccountsInfo(userId: Int): List<AccountEntity> {
        return transaction {
            (AccountsToUsers innerJoin Accounts)
                .slice(Accounts.accountId, Accounts.amount, Accounts.curCode)
                .select { (AccountsToUsers.userId eq userId) and (AccountsToUsers.accountId eq Accounts.accountId) }
                .map { mapToAccountsEntity(it) }
        }
    }

    fun createUser(userTo: UserRequest): Int {
        return transaction {
            val createdUserId = Users.insert {
                it[firstName] = userTo.firstName
                it[lastName] = userTo.lastName
            } get Users.userId

            try {
                if (userTo.accountIds != null) {
                    userTo.accountIds.stream()
                        .filter { accountId -> validateAccountIdExists(accountId) }
                        .forEach { id ->
                            AccountsToUsers.insert {
                                it[userId] = createdUserId
                                it[accountId] = id
                            }
                        }
                }
            } catch(ex: ExposedSQLException) {
                if (ex.message?.contains("Unique index or primary key violation")!!) {
                    throw AccountsException("Try to assign already assigned account")
                } else {
                    throw ex
                }
            }
            createdUserId
        }
    }

    fun deleteUser(userId: Int): Int {
        return transaction {

            val countOfDeleted = Users.deleteWhere {
                Users.userId eq userId
            }

            if (countOfDeleted > 0) {
                AccountsToUsers.deleteWhere {
                    AccountsToUsers.userId eq userId
                }
            }

            countOfDeleted
        }
    }

    private fun validateAccountIdExists(accountId: Int): Boolean {
        return transaction {
            var isPresent = false
            val existingAccount: Int = Accounts
                .select { Accounts.accountId eq accountId }.count()
            if (existingAccount > 0) {
                isPresent = true
            }
            isPresent
        }
    }
}