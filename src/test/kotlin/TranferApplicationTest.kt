import com.soyaburritos.api.db.Accounts
import com.soyaburritos.api.db.AccountsToUsers
import com.soyaburritos.api.db.Users
import com.soyaburritos.api.module.module
import configs.prepareData
import io.ktor.application.Application
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.stop
import io.ktor.server.netty.Netty
import io.restassured.RestAssured
import io.restassured.response.ResponseBodyExtractionOptions
import io.restassured.specification.RequestSpecification
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

open class TranferApplicationTest {

    protected fun RequestSpecification.When(): RequestSpecification {
        return this.`when`()
    }

    protected inline fun <reified T> ResponseBodyExtractionOptions.to(): T {
        return this.`as`(T::class.java)
    }

    companion object {

        const val USERS_SIZE = 3
        const val ACCOUNTS_SIZE = 6
        const val USER_NAME = "USER_NEW"
        const val USER_LAST_NAME = "Userovich"
        const val EXISTING_USER_ID = 1
        const val NOT_EXISTING_USER_ID = 123
        const val EXISTING_ACCOUNT_ID = 1
        const val NOT_EXISTING_ACCOUNT_ID = 123
        const val EXISTING_ACCOUNT_CUR_CODE = "RUB"
        const val CUR_CODE = "RUB"
        const val WRONG_CUR_CODE = "TST"
        const val WRONG_ID = "testWrongId"

        val EXISTING_ACCOUNT_BALANCE = BigDecimal("100.0000")
        val ACCOUNT_BALANCE_TO_CREATE = BigDecimal("10.0000")
        val AMOUNT = BigDecimal("30.0000")

        // for transfer test
        const val ACCOUNT_ID_FROM = 1
        const val ACCOUNT_ID_TO = 2
        const val ACCOUNTS_CUR_CODE = "RUB"
        const val TRANSFER_CUR_CODE = "EUR"
        val INITIAL_BALANCE_FROM = BigDecimal("100.0000")
        val INITIAL_BALANCE_TO = BigDecimal("200.0000")
        val TRANSFER_AMOUNT = BigDecimal("30.0000")

        private var serverStarted = false

        private lateinit var server: ApplicationEngine

        @BeforeAll
        @JvmStatic
        fun startServer() {
            if (!serverStarted) {
                server = embeddedServer(Netty, 8080, module = Application::module)
                server.start()
                serverStarted = true

                RestAssured.baseURI = "http://localhost"
                RestAssured.port = 8080
                Runtime.getRuntime().addShutdownHook(Thread { server.stop(0, 0, TimeUnit.SECONDS) })
            }
        }
    }

    @BeforeEach
    fun before() = runBlocking {
        newSuspendedTransaction {
            Users.deleteAll()
            Accounts.deleteAll()
            AccountsToUsers.deleteAll()
            prepareData()
        }
    }
}