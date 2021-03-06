package com.soyaburritos.api.controllers

import com.soyaburritos.api.entities.UserRequest
import com.soyaburritos.api.services.UsersService
import com.soyaburritos.api.validators.validateUserId
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*

internal fun Routing.apiUsers(usersService: UsersService) {
    route("/users") {
        get("/all") {
            val withAccountInfo: Boolean? = call.request.queryParameters["withAccountsInfo"]?.toBoolean()
            call.respond(usersService.getAllUsers(withAccountInfo))
        }
        get("/{userId}") {
            val userId: Int? = call.parameters["userId"]?.toIntOrNull()
            val withAccountInfo: Boolean? = call.request.queryParameters["withAccountsInfo"]?.toBoolean()

            validateUserId(userId)

            val user =
                userId?.let { usersService.getUser(userId, withAccountInfo) }

            user?.let { call.respond(user) } ?: call.respond(HttpStatusCode.NotFound)
        }

        post("/create") {
            val userToCreate = call.receive<UserRequest>()

            call.respond(usersService.createUser(userToCreate))
        }

        delete("/{userId}") {
            val userId: Int? = call.parameters["userId"]?.toIntOrNull()

            validateUserId(userId)

            userId?.let {
                val deleteCount = usersService.deleteUser(userId)
                if (deleteCount == 1) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}

