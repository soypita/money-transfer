package controllers

import TranferApplicationTest
import com.soyaburritos.api.entities.AccountEntity
import io.restassured.RestAssured
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class AccountsControllerTest : TranferApplicationTest()  {
    @Test
    fun getAllAccountsTest() {
        // given

        // when
        val users = RestAssured.get("/accounts/all").then().extract().to<List<AccountEntity>>()

        // then
        Assertions.assertThat(users.size).isEqualTo(ACCOUNTS_SIZE)
    }
}