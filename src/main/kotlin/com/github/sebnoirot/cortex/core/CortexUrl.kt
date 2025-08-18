package com.github.sebnoirot.cortex.core

import java.net.URI

internal object CortexUrl {

    /**
     * Normalize user-provided base URL.
     * - trims whitespace
     * - ensures scheme (defaults to https)
     * - strips trailing slash
     * Returns null when invalid.
     */
    fun normalizeBaseUrl(input: String?): String? {
        val raw = input?.trim().orEmpty()
        if (raw.isEmpty()) return null
        val withScheme = if (raw.startsWith("http://") || raw.startsWith("https://")) raw else "https://$raw"
        return try {
            val uri = URI(withScheme)
            if (uri.scheme != "http" && uri.scheme != "https") return null
            if (uri.host.isNullOrBlank()) return null
            val normalized = withScheme.removeSuffix("/")
            normalized
        } catch (_: Exception) {
            null
        }
    }

    fun build(baseUrl: String, path: String): String {
        val b = baseUrl.removeSuffix("/")
        val p = if (path.startsWith("/")) path else "/$path"
        return b + p
    }
}
