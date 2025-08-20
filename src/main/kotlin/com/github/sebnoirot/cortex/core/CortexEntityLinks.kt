package com.github.sebnoirot.cortex.core

internal object CortexEntityLinks {

    fun serviceUrl(baseUrl: String, id: String): String =
        CortexUrl.build(baseUrl, "/services/$id")

    fun teamUrl(baseUrl: String, id: String): String =
        CortexUrl.build(baseUrl, "/teams/$id")

    fun scorecardUrl(baseUrl: String, id: String): String =
        CortexUrl.build(baseUrl, "/scorecards/$id")
}
