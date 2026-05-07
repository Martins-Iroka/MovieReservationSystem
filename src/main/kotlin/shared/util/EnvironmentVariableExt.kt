package com.martdev.shared.util

import io.ktor.server.application.*

fun ApplicationEnvironment.getEnvValue(key: String) = config.property(key).getString()