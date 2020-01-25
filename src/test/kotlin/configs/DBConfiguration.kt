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
    val firstAccount = AccountEntity(null, BigDecimal("100.0000"), "RUB")
    val secondAccount = AccountEntity(null, BigDecimal("200.0000"), "RUB")
    val thirdAccount = AccountEntity(null, BigDecimal("50.0000"), "RUB")
    val fourthAccount = AccountEntity(null, BigDecimal("10.0000"), "RUB")
    val fifthAccount = AccountEntity(null, BigDecimal("500.0000"), "RUB")
    val sixAccount = AccountEntity(null, BigDecimal("30.0000"), "RUB")

    val accounts = listOf(
        firstAccount,
        secondAccount,
        thirdAccount,
        fourthAccount,
        fifthAccount,
        sixAccount
    )

    val users = listOf(
        UserWithAccountInfoEntity(null, "Test", "Testov", listOf(firstAccount, sixAccount)),
        UserWithAccountInfoEntity(null, "John", "Smith", listOf(secondAccount, fifthAccount, thirdAccount)),
        UserWithAccountInfoEntity(null, "David", "Gilmor", listOf(fourthAccount))
    )

    transaction {
        Accounts.batchInsert(accounts) {
            this[Accounts.amount] = it.amount
            this[Accounts.curCode] = it.curCode
        }

        Users.batchInsert(users) {
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