package com.soyaburritos.api

import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty


class TransferApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val server = embeddedServer(Netty, commandLineEnvironment(args))
            server.start(wait = true)
        }
    }
}
