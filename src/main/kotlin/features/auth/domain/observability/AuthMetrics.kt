package com.martdev.features.auth.domain.observability

interface AuthMetrics {
    fun count(name: String, vararg tags: Pair<String, String>)
}
