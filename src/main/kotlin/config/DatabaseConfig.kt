package com.martdev.config

data class DatabaseConfig(
    val address: String = "",
    val user: String = "",
    val password: String = "",
    val maxOpenCon: Int = 0,
    val maxIdleCon: Int = 0,
    val maxIdleTime: Long = 0
)
