package com.soyaburritos.api.configuration

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigUtil
import io.ktor.config.ApplicationConfig
import io.ktor.config.HoconApplicationConfig
import kotlin.math.min

private val ktorDeploymentConfig: ApplicationConfig =
    HoconApplicationConfig(ConfigFactory.load().getConfig(ConfigUtil.joinPath("ktor", "database")))

// Database configs
val DB_DRIVER = ktorDeploymentConfig.property("driver").getString()
val DB_URL = ktorDeploymentConfig.property("url").getString()
val DB_USER = ktorDeploymentConfig.property("user").getString()
val DB_PASSWORD = ktorDeploymentConfig.property("password").getString()

// Connection pool configs
const val MAX_BACKOFF_MS = 16000L
val DEFAULT_BACKOFF_SEQUENCE_MS = generateSequence(1000L) { min(it * 2, MAX_BACKOFF_MS) }

// Check for test environment
val IS_TEST: Boolean =
    HoconApplicationConfig(
        ConfigFactory.load().getConfig(
            ConfigUtil.joinPath(
                "ktor",
                "deployment"
            )
        )
    ).property("environment").getString() == "test"