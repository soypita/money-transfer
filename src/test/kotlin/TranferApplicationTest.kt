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
        val AMOUNT = BigDecimal("30.0000")
        val CUR_CODE = "RUB"

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