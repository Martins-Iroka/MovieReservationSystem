package features.utils

import com.martdev.plugins.configureRateLimiter
import com.martdev.plugins.configureRequestValidation
import com.martdev.plugins.configureSecurity
import com.martdev.plugins.configureSerialization
import com.martdev.plugins.configureStatusPages
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.core.module.Module
import org.koin.ktor.plugin.Koin

inline fun Application.testAppConfiguration(module: Module, crossinline block: Route.() -> Unit) {
    install(Koin) {
        modules(module)
    }
    configureSerialization()
    configureStatusPages()
    configureSecurity()
    configureRateLimiter()
    configureRequestValidation()
    routing {
        block()
    }
}