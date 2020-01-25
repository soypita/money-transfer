package com.soyaburritos.api.configuration

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigUtil
import io.ktor.config.ApplicationConfig
import io.ktor.config.HoconApplicationConfig

private val ktorDeploymentConfig: ApplicationConfig =
    HoconApplicationConfig(ConfigFactory.load().getConfig(ConfigUtil.joinPath("ktor", "database")))

val DB_DRIVER = ktorDeploymentConfig.property("driver").getString()
val DB_URL = ktorDeploymentConfig.property("url").getString()
val DB_USER = ktorDeploymentConfig.property("user").getString()
val DB_PASSWORD = ktorDeploymentConfig.property("password").getString()
