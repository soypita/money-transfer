package controllers

import TranferApplicationTest
import com.soyaburritos.api.converters.mapToAccountsEntity
import com.soyaburritos.api.db.Accounts
import com.soyaburritos.api.entities.MoneyTransaction
import io.ktor.http.HttpStatusCode
import io.restassured.RestAssured.*
import io.restassured.http.ContentType
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import org.junit.jupiter.api.Test

class TransferControllerTest : TranferApplicationTest() {
    private val BASE_URL = "/transfer"

    @Test
    fun shouldSuccessfullyTransferMoney() {
        // given
        val transferTransaction = MoneyTransaction(TRANSFER_AMOUNT, ACCOUNTS_CUR_CODE, ACCOUNT_ID_FROM, ACCOUNT_ID_TO)

        // when
        given()
            .contentType(ContentType.JSON)
            .body(transferTransaction)
            .When()
            .post(BASE_URL)
            .then()
            .statusCode(HttpStatusCode.OK.value)

        // then
        runBlocking {
            val accountFrom = newSuspendedTransaction {
                Accounts.select { Accounts.accountId eq ACCOUNT_ID_FROM }
                    .map { mapToAccountsEntity(it) }
                    .first()
            }

            val accountTo = newSuspendedTransaction {
                Accounts.select { Accounts.accountId eq ACCOUNT_ID_TO }
                    .map { mapToAccountsEntity(it) }
                    .first()
            }

            assertThat(accountFrom.amount).isEqualTo(INITIAL_BALANCE_FROM.subtract(TRANSFER_AMOUNT))
            assertThat(accountTo.amount).isEqualTo(INITIAL_BALANCE_TO.add(TRANSFER_AMOUNT))
        }
    }

    @Test
    fun failedToTransferWhenZeroAccountIdIsPassed() {
        // given
        val transferTransaction = MoneyTransaction(TRANSFER_AMOUNT, ACCOUNTS_CUR_CODE, 0, ACCOUNT_ID_TO)

        // when
        given()
            .contentType(ContentType.JSON)
            .body(transferTransaction)
            .When()
            .post(BASE_URL)
            .then()
            .statusCode(HttpStatusCode.InternalServerError.value)

    }

    @Test
    fun failedToTransferWhenNotExistingAccountPassed() {
        // given
        val transferTransaction =
            MoneyTransaction(TRANSFER_AMOUNT, ACCOUNTS_CUR_CODE, NOT_EXISTING_ACCOUNT_ID, ACCOUNT_ID_TO)

        // when
        given()
            .contentType(ContentType.JSON)
            .body(transferTransaction)
            .When()
            .post(BASE_URL)
            .then()
            .statusCode(HttpStatusCode.InternalServerError.value)
    }

    @Test
    fun failedToTransferWhenTransferCurrCodeNotMatchWithAccountFrom() {
        // given
        val transferTransaction =
            MoneyTransaction(TRANSFER_AMOUNT, TRANSFER_CUR_CODE, ACCOUNT_ID_FROM, ACCOUNT_ID_TO)

        // when
        given()
            .contentType(ContentType.JSON)
            .body(transferTransaction)
            .When()
            .post(BASE_URL)
            .then()
            .statusCode(HttpStatusCode.InternalServerError.value)
    }

    @Test
    fun failedToTransferWhenWrongTransferCurrCodePassed() {
        // given
        val transferTransaction =
            MoneyTransaction(TRANSFER_AMOUNT, WRONG_CUR_CODE, ACCOUNT_ID_FROM, ACCOUNT_ID_TO)

        // when
        given()
            .contentType(ContentType.JSON)
            .body(transferTransaction)
            .When()
            .post(BASE_URL)
            .then()
            .statusCode(HttpStatusCode.InternalServerError.value)
    }

    @Test
    fun failedToTransferWhenNotEnoughMoneyLeftOnAccountFrom() {
        // given
        val transferTransaction =
            MoneyTransaction(
                INITIAL_BALANCE_FROM.add(TRANSFER_AMOUNT),
                ACCOUNTS_CUR_CODE,
                ACCOUNT_ID_FROM,
                ACCOUNT_ID_TO
            )

        // when
        given()
            .contentType(ContentType.JSON)
            .body(transferTransaction)
            .When()
            .post(BASE_URL)
            .then()
            .statusCode(HttpStatusCode.InternalServerError.value)
    }

    @Test
    fun failedToTransferWhenCurCodesOfAccountsMismatch() {
        // given
        runBlocking {
            newSuspendedTransaction {
                Accounts.update({ Accounts.accountId eq ACCOUNT_ID_FROM }) {
                    it[curCode] = TRANSFER_CUR_CODE
                }
            }
        }
        val transferTransaction =
            MoneyTransaction(TRANSFER_AMOUNT, TRANSFER_CUR_CODE, ACCOUNT_ID_FROM, ACCOUNT_ID_TO)

        // when
        given()
            .contentType(ContentType.JSON)
            .body(transferTransaction)
            .When()
            .post(BASE_URL)
            .then()
            .statusCode(HttpStatusCode.InternalServerError.value)

    }
}