package configs

import com.soyaburritos.api.db.Accounts
import com.soyaburritos.api.db.AccountsToUsers
import com.soyaburritos.api.db.Users
import com.soyaburritos.api.entities.AccountEntity
import com.soyaburritos.api.entities.UserWithAccountInfoEntity
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

fun prepareData() {
    val firstAccount = AccountEntity(1, BigDecimal("100.0000"), "RUB")
    val secondAccount = AccountEntity(2, BigDecimal("200.0000"), "RUB")
    val thirdAccount = AccountEntity(3, BigDecimal("50.0000"), "RUB")
    val fourthAccount = AccountEntity(4, BigDecimal("10.0000"), "RUB")
    val fifthAccount = AccountEntity(5, BigDecimal("500.0000"), "RUB")
    val sixAccount = AccountEntity(6, BigDecimal("30.0000"), "RUB")

    val accounts = listOf(
        firstAccount,
        secondAccount,
        thirdAccount,
        fourthAccount,
        fifthAccount,
        sixAccount
    )

    val users = listOf(
        UserWithAccountInfoEntity(1, "Test", "Testov", listOf(firstAccount, sixAccount)),
        UserWithAccountInfoEntity(2, "John", "Smith", listOf(secondAccount, fifthAccount, thirdAccount)),
        UserWithAccountInfoEntity(3, "David", "Gilmor", listOf(fourthAccount))
    )

    transaction {
        Accounts.batchInsert(accounts) {
            this[Accounts.accountId] = it.accountId!!
            this[Accounts.amount] = it.amount
            this[Accounts.curCode] = it.curCode
        }

        Users.batchInsert(users) {
            this[Users.userId] = it.userId!!
            this[Users.firstName] = it.firstName
            this[Users.lastName] = it.lastName
        }

        users.forEach { user ->
            val userId = Users
                .slice(Users.userId)
                .select { (Users.firstName eq user.firstName) and (Users.lastName eq user.lastName) }
                .first()[Users.userId]

            user.accounts.forEach{ account ->
                val accountId = Accounts
                    .slice(Accounts.accountId)
                    .select { (Accounts.amount eq account.amount) }
                    .first()[Accounts.accountId]

                AccountsToUsers.insert {
                    it[AccountsToUsers.accountId] = accountId
                    it[AccountsToUsers.userId] = userId
                }
            }
        }
    }
}