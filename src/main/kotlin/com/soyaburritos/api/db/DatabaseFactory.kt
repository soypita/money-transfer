package com.soyaburritos.api.db

import com.soyaburritos.api.configuration.*
import com.soyaburritos.api.entities.AccountEntity
import com.soyaburritos.api.entities.UserWithAccountInfoEntity
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal

object DatabaseFactory {
    private val log: Logger = LoggerFactory.getLogger(DatabaseFactory::class.java)

    fun init() {
        log.info("Try to init DB")
        val hikariDataSource = createHikariDataSourceWithRetry(
            jdbcUrl = DB_URL,
            username = DB_USER,
            password = DB_PASSWORD,
            driver = DB_DRIVER
        )

        val db = Database.connect(hikariDataSource)
        db.useNestedTransactions = true

        if (IS_TEST) {
            createSchemaAndPrepareData()
        }
    }

    tailrec fun createHikariDataSourceWithRetry(
        jdbcUrl: String, username: String, password: String,
        driver: String,
        backoffSequenceMs: Iterator<Long> = DEFAULT_BACKOFF_SEQUENCE_MS.iterator()
    ): HikariDataSource {
        try {
            val config = HikariConfig()
            config.jdbcUrl = jdbcUrl
            config.username = username
            config.password = password
            config.driverClassName = driver
            return HikariDataSource(config)
        } catch (ex: Exception) {
            log.error("Failed to create data source ${ex.message}")
            if (!backoffSequenceMs.hasNext()) throw ex
        }
        val backoffMillis = backoffSequenceMs.next()
        log.info("Trying again in $backoffMillis millis")
        Thread.sleep(backoffMillis)
        return createHikariDataSourceWithRetry(jdbcUrl, username, password, driver, backoffSequenceMs)
    }


    private fun createSchemaAndPrepareData() {
        transaction {
            SchemaUtils.drop(Accounts)
            SchemaUtils.drop(Users)
            SchemaUtils.drop(AccountsToUsers)
        }

        transaction {
            log.info("Creating/Updating schema")

            SchemaUtils.create(Accounts, Users, AccountsToUsers)
        }

        val existingUsersCount = transaction {
            Users.selectAll().count()
        }

        if (existingUsersCount > 0) {
            log.info("Data already exist")
            return
        }

        log.info("Prepare Users and Accounst")

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

                user.accounts.forEach { account ->
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
}