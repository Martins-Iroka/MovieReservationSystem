package com.martdev.config

import com.martdev.shared.util.getEnvValue
import io.ktor.server.application.*

data class StytchConfig(
    val projectId: String = "",
    val secret: String = ""
) {
    companion object {
        fun fromEnvironment(environment: ApplicationEnvironment): StytchConfig {
            return StytchConfig(
                projectId = environment.getEnvValue("stytch.stytchID"),
                secret = environment.getEnvValue("stytch.stytchSecret")
            )
        }
    }
}
