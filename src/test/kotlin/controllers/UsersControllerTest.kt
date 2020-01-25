package controllers

import io.restassured.RestAssured.*
import TranferApplicationTest
import com.soyaburritos.api.converters.mapToUserEntity
import com.soyaburritos.api.db.Accounts
import com.soyaburritos.api.db.AccountsToUsers
import com.soyaburritos.api.db.Users
import com.soyaburritos.api.entities.UserRequest
import com.soyaburritos.api.entities.UserWithAccountInfoEntity
import io.ktor.http.HttpStatusCode
import io.restassured.http.ContentType
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class UsersControllerTest : TranferApplicationTest() {
    private val BASE_URL = "/users"

    @Test
    fun getAllUsersWithoutAccountInfoTest() {
        // when
        val users = get("${BASE_URL}/all")
            .then()
            .extract()
            .body()
            .jsonPath().getList("", UserWithAccountInfoEntity::class.java)

        // then
        assertThat(users.size).isEqualTo(USERS_SIZE)
    }

    @Test
    fun getAllUsersWithAccountInfoTest() {
        // when
        val users = given().queryParam("withAccountsInfo", "true")
            .get("${BASE_URL}/all")
            .then()
            .extract()
            .body()
            .jsonPath()
            .getList("", UserWithAccountInfoEntity::class.java)

        // then
        assertThat(users.size).isEqualTo(USERS_SIZE)
        users.forEach {
            assertThat(it.accounts).isNotEmpty
        }
    }

    @Test
    fun getUserByIdWithoutAccountInfo() {
        // when
        val user = given().queryParam("withAccountsInfo", "false")
            .get("${BASE_URL}/${EXISTING_USER_ID}")
            .then()
            .extract()
            .to<UserWithAccountInfoEntity>()

        // then
        assertThat(user).isNotNull
        assertThat(user.userId).isEqualTo(EXISTING_USER_ID)
        assertThat(user.accounts).isEmpty()
    }

    @Test
    fun getUserByIdWithAccountInfo() {
        // when
        val user = given().queryParam("withAccountsInfo", "true")
            .get("${BASE_URL}/${EXISTING_USER_ID}")
            .then()
            .extract()
            .to<UserWithAccountInfoEntity>()

        // then
        assertThat(user).isNotNull
        assertThat(user.userId).isEqualTo(EXISTING_USER_ID)
        assertThat(user.accounts).isNotEmpty
    }

    @Test
    fun getNotExistingUserShouldReturnNotFoundError() {
        // expect
        get("${BASE_URL}/${NOT_EXISTING_USER_ID}")
            .then()
            .statusCode(HttpStatusCode.NotFound.value)
    }

    @Test
    fun failedGetUserWhenBadIdIsPassed() {
        // expect
        get("${BASE_URL}/${WRONG_ID}")
            .then()
            .statusCode(HttpStatusCode.InternalServerError.value)
    }

    @Test
    fun createUserWithoutAccountsTest() {
        // given
        val userToCreate = UserRequest(USER_NAME, USER_LAST_NAME, null)
        // when
        val newUserId = given()
            .contentType(ContentType.JSON)
            .body(userToCreate)
            .When()
            .post("${BASE_URL}/create")
            .then()
            .statusCode(HttpStatusCode.OK.value)
            .extract().to<Int>()

        // then
        val createdUser = runBlocking {
            newSuspendedTransaction {
                Users.select { Users.userId eq newUserId }
                    .map { mapToUserEntity(it) }
                    .firstOrNull()
            }
        }

        assertThat(createdUser).isNotNull
        assertThat(createdUser?.firstName).isEqualTo(USER_NAME)
        assertThat(createdUser?.lastName).isEqualTo(USER_LAST_NAME)
    }

    @Test
    fun createUserWithExistingAccountTest() {
        // given
        val newAccountId = runBlocking {
            newSuspendedTransaction {
                Accounts.insert {
                    it[amount] = AMOUNT
                    it[curCode] = CUR_CODE
                } get Accounts.accountId
            }
        }

        val userToCreate = UserRequest(USER_NAME, USER_LAST_NAME, listOf(newAccountId))

        // when
        val newUserId = given()
            .contentType(ContentType.JSON)
            .body(userToCreate)
            .When()
            .post("${BASE_URL}/create")
            .then()
            .statusCode(HttpStatusCode.OK.value)
            .extract().to<Int>()

        // then
        val createdUser = runBlocking {
            newSuspendedTransaction {
                Users.select { Users.userId eq newUserId }
                    .map { mapToUserEntity(it) }
                    .firstOrNull()
            }
        }

        val addedAccount = runBlocking {
            newSuspendedTransaction {
                AccountsToUsers.select {
                    (AccountsToUsers.userId eq newUserId) and (AccountsToUsers.accountId eq newAccountId)
                }.count()
            }
        }

        assertThat(createdUser).isNotNull
        assertThat(createdUser?.firstName).isEqualTo(USER_NAME)
        assertThat(createdUser?.lastName).isEqualTo(USER_LAST_NAME)
        assertThat(addedAccount).isEqualTo(1)
    }

    @Test
    fun creatingUserWithAlreadyAssigningAccount() {
        // given
        val userToUpdate = UserRequest(USER_NAME, USER_LAST_NAME, listOf(EXISTING_ACCOUNT_ID))

        // then
        given()
            .contentType(ContentType.JSON)
            .body(userToUpdate)
            .When()
            .post("${BASE_URL}/create")
            .then()
            .statusCode(HttpStatusCode.InternalServerError.value)
    }

    @Test
    fun deleteExistingUserWithAllConnectedAccountsTest() {
        // expect
        delete("${BASE_URL}/${EXISTING_USER_ID}")
            .then()
            .statusCode(HttpStatusCode.OK.value)
    }

    @Test
    fun deleteNotExistingUserShouldReturnNotFoundError() {
        // expect
        delete("${BASE_URL}/${NOT_EXISTING_USER_ID}")
            .then()
            .statusCode(HttpStatusCode.NotFound.value)
    }

    @Test
    fun failedToDeleteWhenWrongUserIdIsPassed() {
        // expect
        delete("${BASE_URL}/${WRONG_ID}")
            .then()
            .statusCode(HttpStatusCode.InternalServerError.value)
    }
}