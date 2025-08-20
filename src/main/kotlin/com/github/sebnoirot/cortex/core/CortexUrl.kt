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

    /**
     * Derive the Cortex API base URL from the UI base (product) URL.
     * Example: https://cortex.example.com -> https://api.cortex.example.com
     * If the host already starts with "api.", it is returned as-is.
     */
    fun deriveApiBase(uiBaseUrl: String): String {
        val uri = URI(uiBaseUrl)
        val host = uri.host
        val apiHost = if (host.startsWith("api.")) host else "api.$host"
        val scheme = uri.scheme ?: "https"
        val portPart = if (uri.port != -1) ":${uri.port}" else ""
        return "$scheme://$apiHost$portPart"
    }

    fun build(baseUrl: String, path: String): String {
        val b = baseUrl.removeSuffix("/")
        val p = if (path.startsWith("/")) path else "/$path"
        return b + p
    }
}
