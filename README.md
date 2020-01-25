**money-transfer-api**

**How to build**

`./gradlew clean build`

**How to run**

`./gradlew run`

Run server on localhost:8080 by default

**How to test**

`./gradlew clean test`

**API description**

**USERS**

<ul>

<li>Get all users <br/>

GET `http://localhost:8080/users/all`
</li>

<li>
Get all users with accounts info </br>

GET `http://localhost:8080/users/all?withAccountsInfo=true`
</li>

<li>
Get user by id with accounts info </br>

GET `http://localhost:8080/users/{userId}?withAccountsInfo=true`
</li>

<li>
Create new user </br>

POST `http://localhost:8080/users/create`

request: 
```json
{
  "firstName": "Testing",
  "lastName": "Test",
  "accountIds": [1, 2, 3]
}
```
</li>

<li>
Delete user </br>

DELETE `http://localhost:8080/users/{userId}`
</li>

</ul>

**ACCOUNTS**

<ul>
<li>
Get all accounts <br/>

GET `http://localhost:8080/accounts/all`
</li>
<li>
Get account by accountId </br>

GET `http://localhost:8080/accounts/{accountId}`
</li>

<li>
Get account balance

GET `http://localhost:8080/accounts/{accountId}/balance`
</li>

<li>
Create new account (without assigning to user) <br/>

POST `http://localhost:8080/accounts/create`

request:
```json
{
  "amount": 1000.000,
  "curCode": "EUR"
}
```
</li>

<li>
Deposit to account </br>

POST `http://localhost:8080/accounts/{accountId}/deposit/{amount}`
</li>
<li>
Withdraw to account </br>

POST `http://localhost:8080/accounts/{accountId}/withdraw/{amount}`
</li>
<li>
Asignee account to user</br>

POST `http://localhost:8080/accounts/{accountId}/addToUser/{userId}`
</li>

<li>
Delete account</br>

DELETE `http://localhost:8080/accounts/{accountId}`
</li>
</ul>

**TRANSFER**

<ul>
<li>
Transfer funds from one account to another

POST `http://localhost:8080/transfer`

request:
```json
{
  "amount": 1000.000,
  "curCode": "RUB",
  "fromAccountId": 1,
  "toAccountId": 2
}
```
</li>
</ul>