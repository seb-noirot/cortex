package com.github.sebnoirot.cortex.core

import com.intellij.openapi.diagnostic.Logger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

internal object CortexHealthClient {
    private val log = Logger.getInstance(CortexHealthClient::class.java)

    data class Result(val ok: Boolean, val message: String, val version: String? = null, val code: Int? = null)

    private val client: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build()

    fun check(baseUrl: String, token: String?): Result {
        val url = try {
            CortexUrl.build(baseUrl, "/api/version")
        } catch (t: Throwable) {
            return Result(false, "Invalid base URL: ${t.message}")
        }

        return sendGet(url, token)
    }

    /**
     * Check the Catalog endpoint on the API base by calling /api/v1/catalog/definitions.
     * 200 status is considered a successful connection.
     */
    fun checkApi(apiBaseUrl: String, token: String?): Result {
        val url = try {
            CortexUrl.build(apiBaseUrl, "/api/v1/catalog/definitions")
        } catch (t: Throwable) {
            return Result(false, "Invalid API base URL: ${t.message}")
        }
        return sendGet(url, token)
    }

    private fun sendGet(url: String, token: String?): Result {
        val builder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(8))
            .GET()
            .header("User-Agent", "Cortex-IntelliJ-Plugin")

        if (!token.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $token")
        }

        return try {
            val response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() in 200..299) {
                Result(true, "Connection successful", null, response.statusCode())
            } else {
                val msg = when (response.statusCode()) {
                    401 -> "Unauthorized (token invalid or missing)"
                    404 -> "Endpoint not found at $url"
                    else -> "HTTP ${response.statusCode()}"
                }
                Result(false, msg, null, response.statusCode())
            }
        } catch (t: Exception) {
            // Do not include token
            log.warn("Health check failed: ${t.message}")
            return Result(false, t.message ?: "Request failed", null, null)
        }
    }
}
