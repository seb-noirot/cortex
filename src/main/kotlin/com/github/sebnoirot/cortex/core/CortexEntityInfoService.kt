package com.github.sebnoirot.cortex.core

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.concurrency.AppExecutorUtil
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

/**
 * Lightweight cache and background fetcher for Cortex entities.
 * Network calls are never made on the EDT.
 */
@Service(Service.Level.APP)
internal class CortexEntityInfoService {
    private val log = Logger.getInstance(CortexEntityInfoService::class.java)

    private val client: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build()

    private data class Entry(val value: String, val expiresAt: Long)

    private val serviceNameCache = ConcurrentHashMap<String, Entry>()

    private val ttlMillis = Duration.ofMinutes(5).toMillis()

    fun getServiceNameCached(id: String): String? {
        val now = System.currentTimeMillis()
        val entry = serviceNameCache[id]
        return if (entry != null && entry.expiresAt > now) entry.value else null
    }

    fun ensureServiceFetchedAsync(id: String) {
        val now = System.currentTimeMillis()
        val entry = serviceNameCache[id]
        if (entry != null && entry.expiresAt > now) return

        AppExecutorUtil.getAppExecutorService().submit {
            try {
                val settings = com.intellij.openapi.application.ApplicationManager.getApplication()
                    .getService(CortexSettings::class.java)
                val creds = com.intellij.openapi.application.ApplicationManager.getApplication()
                    .getService(CortexCredentials::class.java)
                val baseUrl = settings.baseUrl?.let { CortexUrl.normalizeBaseUrl(it) } ?: return@submit
                val token = creds.getToken()

                val url = CortexUrl.build(baseUrl, "/api/services/$id")
                val reqBuilder = HttpRequest.newBuilder()
                    .GET()
                    .timeout(Duration.ofSeconds(8))
                    .uri(URI.create(url))
                    .header("User-Agent", "Cortex-IntelliJ-Plugin")
                if (!token.isNullOrBlank()) {
                    reqBuilder.header("Authorization", "Bearer $token")
                }
                val response = client.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofString())
                if (response.statusCode() in 200..299) {
                    val name = parseNameFromJson(response.body())
                    if (!name.isNullOrBlank()) {
                        serviceNameCache[id] = Entry(name, now + ttlMillis)
                    }
                } else {
                    // Fail softly; do not log token
                    log.debug("Service info fetch failed: HTTP ${response.statusCode()}")
                }
            } catch (t: Throwable) {
                log.debug("Service info fetch error: ${t.message}")
            }
        }
    }

    private fun parseNameFromJson(body: String): String? {
        // Minimal parsing without dependencies: extract name via regex
        val regex = Regex("\"name\"\\s*:\\s*\"([^\"]+)\"")
        val match = regex.find(body) ?: return null
        return match.groupValues.getOrNull(1)
    }
}
