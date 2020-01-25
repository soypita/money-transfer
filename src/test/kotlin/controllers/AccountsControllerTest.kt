package controllers

import TranferApplicationTest
import com.soyaburritos.api.converters.mapToAccountsEntity
import com.soyaburritos.api.db.Accounts
import com.soyaburritos.api.entities.AccountEntity
import io.ktor.http.HttpStatusCode
import io.restassured.RestAssured.*
import io.restassured.http.ContentType
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class AccountsControllerTest : TranferApplicationTest() {
    private val BASE_URL = "/accounts"

    @Test
    fun getAllAccountsTest() {
        // when
        val accounts = get("${BASE_URL}/all")
            .then()
            .extract()
            .to<List<AccountEntity>>()

        // then
        assertThat(accounts.size).isEqualTo(ACCOUNTS_SIZE)
    }

    @Test
    fun getAccountByAccountId() {
        // when
        val account = get("${BASE_URL}/${EXISTING_ACCOUNT_ID}")
            .then()
            .extract()
            .to<AccountEntity>()

        // then
        assertThat(account.curCode).isEqualTo(EXISTING_ACCOUNT_CUR_CODE)
    }

    @Test
    fun getNotExistingAccount() {
        // expect
        get("${BASE_URL}/${NOT_EXISTING_ACCOUNT_ID}")
            .then()
            .statusCode(HttpStatusCode.NotFound.value)
    }

    @Test
    fun failedToGetAccountWhenBadAccountIdPassed() {
        // expect
        get("${BASE_URL}/testWrongAccId")
            .then()
            .statusCode(HttpStatusCode.InternalServerError.value)
    }

    @Test
    fun getExistingAccountBalance() {
        // when
        val accountBalance = get("${BASE_URL}/${EXISTING_ACCOUNT_ID}/balance")
            .then()
            .extract()
            .to<BigDecimal>()

        // then
        assertThat(accountBalance).isEqualTo(EXISTING_ACCOUNT_BALANCE)
    }

    @Test
    fun getNotExistingAccountBalance() {
        // expect
        get("${BASE_URL}/${NOT_EXISTING_ACCOUNT_ID}/balance")
            .then()
            .statusCode(HttpStatusCode.NotFound.value)
    }


    @Test
    fun failedToGetAccountBalanceWhenBadAccountIdPassed() {
        // expect
        get("${BASE_URL}/${WRONG_ID}/balance")
            .then()
            .statusCode(HttpStatusCode.InternalServerError.value)
    }

    @Test
    fun createAccountSuccessfully() {
        // given
        val accountToCreate = AccountEntity(null, ACCOUNT_BALANCE_TO_CREATE, CUR_CODE)

        // when
        val createdAccountId = given()
            .contentType(ContentType.JSON)
            .body(accountToCreate)
            .When()
            .post("${BASE_URL}/create")
            .then()
            .statusCode(HttpStatusCode.OK.value)
            .extract().to<Int>()

        // then
        val createdAccount = runBlocking {
            newSuspendedTransaction {
                Accounts.select { Accounts.accountId eq createdAccountId }
                    .map { mapToAccountsEntity(it) }
                    .firstOrNull()
            }
        }

        assertThat(createdAccount).isNotNull
        assertThat(createdAccount?.curCode).isEqualTo(CUR_CODE)
        assertThat(createdAccount?.amount).isEqualTo(ACCOUNT_BALANCE_TO_CREATE)
    }

    @Test
    fun shouldFailCreateAccountWhenWrongCurCodeIsPassed() {
        // given
        val accountToCreate = AccountEntity(null, ACCOUNT_BALANCE_TO_CREATE, WRONG_CUR_CODE)

        // expect
        given()
            .contentType(ContentType.JSON)
            .body(accountToCreate)
            .When()
            .post("${BASE_URL}/create")
            .then()
            .statusCode(HttpStatusCode.InternalServerError.value)
    }

    @Test
    fun shouldDepositToAccountSuccessfully() {
        // when
        val updatedAccount = given()
            .When()
            .post("${BASE_URL}/${EXISTING_ACCOUNT_ID}/deposit/${AMOUNT}")
            .then()
            .statusCode(HttpStatusCode.OK.value)
            .extract()
            .to<AccountEntity>()

        // then
        assertThat(updatedAccount.accountId).isEqualTo(EXISTING_ACCOUNT_ID)
        assertThat(updatedAccount.curCode).isEqualTo(EXISTING_ACCOUNT_CUR_CODE)
        assertThat(updatedAccount.amount).isEqualTo(EXISTING_ACCOUNT_BALANCE.add(AMOUNT))
    }

    @Test
    fun failedDepositToAccountWhenBadAccountIdIsPassed() {
        // expect
        given()
            .When()
            .post("${BASE_URL}/${WRONG_ID}/deposit/${AMOUNT}")
            .then()
            .statusCode(HttpStatusCode.InternalServerError.value)
    }

    @Test
    fun failedDepositToAccountWhenBadAmountIsPassed() {
        // given
        val badDepositAmount = "wrongAmount"

        // expect
        given()
            .When()
            .post("${BASE_URL}/${EXISTING_ACCOUNT_ID}/deposit/${badDepositAmount}")
            .then()
            .statusCode(HttpStatusCode.InternalServerError.value)
    }

    @Test
    fun failedDepositToNotExistingAccount() {
        // expect
        given()
            .When()
            .post("${BASE_URL}/${NOT_EXISTING_ACCOUNT_ID}/deposit/${AMOUNT}")
            .then()
            .statusCode(HttpStatusCode.NotFound.value)
    }

    @Test
    fun shouldWithdrawFromAccountSuccessfully() {
        // when
        val updatedAccount = given()
            .When()
            .post("${BASE_URL}/${EXISTING_ACCOUNT_ID}/withdraw/${AMOUNT}")
            .then()
            .statusCode(HttpStatusCode.OK.value)
            .extract()
            .to<AccountEntity>()

        // then
        assertThat(updatedAccount.accountId).isEqualTo(EXISTING_ACCOUNT_ID)
        assertThat(updatedAccount.curCode).isEqualTo(EXISTING_ACCOUNT_CUR_CODE)
        assertThat(updatedAccount.amount).isEqualTo(EXISTING_ACCOUNT_BALANCE.subtract(AMOUNT))
    }

    @Test
    fun failedToWithdrawFromAccountWithoutEnoughBalance() {
        given()
            .When()
            .post("${BASE_URL}/${EXISTING_ACCOUNT_ID}/withdraw/${EXISTING_ACCOUNT_BALANCE.add(AMOUNT)}")
            .then()
            .statusCode(HttpStatusCode.InternalServerError.value)
    }

    @Test
    fun failedWithdrawFromAccountWhenBadAccountIdIsPassed() {
        // expect
        given()
            .When()
            .post("${BASE_URL}/${WRONG_ID}/withdraw/${AMOUNT}")
            .then()
            .statusCode(HttpStatusCode.InternalServerError.value)
    }

    @Test
    fun failedWithdrawFromAccountWhenBadAmountIsPassed() {
        // given
        val badDepositAmount = "wrongAmount"

        // expect
        given()
            .When()
            .post("${BASE_URL}/${EXISTING_ACCOUNT_ID}/withdraw/${badDepositAmount}")
            .then()
            .statusCode(HttpStatusCode.InternalServerError.value)
    }

    @Test
    fun failedWithdrawFromNotExistingAccount() {
        // expect
        given()
            .When()
            .post("${BASE_URL}/${NOT_EXISTING_ACCOUNT_ID}/withdraw/${AMOUNT}")
            .then()
            .statusCode(HttpStatusCode.NotFound.value)
    }

    @Test
    fun successfullyAddNewAccountToUser() {
        // given
        val newAccountId = runBlocking {
            newSuspendedTransaction {
                Accounts.insert {
                    it[amount] = AMOUNT
                    it[curCode] = CUR_CODE
                } get Accounts.accountId
            }
        }

        // expect
        given()
            .When()
            .post("${BASE_URL}/${newAccountId}/addToUser/${EXISTING_USER_ID}")
            .then()
            .statusCode(HttpStatusCode.OK.value)
    }

    @Test
    fun failedAddAlreadyAssignedAccountToUser() {
        // expect
        given()
            .When()
            .post("${BASE_URL}/${EXISTING_ACCOUNT_ID}/addToUser/${EXISTING_USER_ID}")
            .then()
            .statusCode(HttpStatusCode.InternalServerError.value)
    }

    @Test
    fun failedAddNotExistingAccountToUser() {
        // expect
        given()
            .When()
            .post("${BASE_URL}/${NOT_EXISTING_ACCOUNT_ID}/addToUser/${EXISTING_USER_ID}")
            .then()
            .statusCode(HttpStatusCode.InternalServerError.value)
    }

    @Test
    fun failedAddAccountToUserWhenBadAccountIdIsPassed() {
        // expect
        given()
            .When()
            .post("${BASE_URL}/${WRONG_ID}/addToUser/${EXISTING_USER_ID}")
            .then()
            .statusCode(HttpStatusCode.InternalServerError.value)
    }

    @Test
    fun failedAddAccountToUserWhenBadUserIdIsPassed() {
        // expect
        given()
            .When()
            .post("${BASE_URL}/${EXISTING_ACCOUNT_ID}/addToUser/${WRONG_ID}")
            .then()
            .statusCode(HttpStatusCode.InternalServerError.value)
    }

    @Test
    fun deleteExistingAccount() {
        // expect
        delete("${BASE_URL}/${EXISTING_ACCOUNT_ID}")
            .then()
            .statusCode(HttpStatusCode.OK.value)
    }

    @Test
    fun failedToDeleteNotExistingAccount() {
        // expect
        delete("${BASE_URL}/${NOT_EXISTING_ACCOUNT_ID}")
            .then()
            .statusCode(HttpStatusCode.NotFound.value)
    }

    @Test
    fun failedToDeleteWhenWrongAccountIdIsPassed() {
        // expect
        delete("${BASE_URL}/${WRONG_ID}")
            .then()
            .statusCode(HttpStatusCode.InternalServerError.value)
    }
}