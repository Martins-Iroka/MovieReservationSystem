package com.martdev.util

import io.ktor.server.application.ApplicationEnvironment

fun ApplicationEnvironment.getEnvValue(key: String) = config.property(key).getString()