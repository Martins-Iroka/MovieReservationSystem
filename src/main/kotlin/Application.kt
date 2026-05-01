package com.martdev

import io.github.cdimascio.dotenv.dotenv

fun main(args: Array<String>) {
    dotenv {
        systemProperties = true
    }
    io.ktor.server.netty.EngineMain.main(args)
}
