package com.github.sebnoirot.cortex

import com.github.sebnoirot.cortex.core.CortexUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CortexUrlTest {

    @Test
    fun normalize_null_or_blank() {
        assertNull(CortexUrl.normalizeBaseUrl(null))
        assertNull(CortexUrl.normalizeBaseUrl(""))
        assertNull(CortexUrl.normalizeBaseUrl("   "))
    }

    @Test
    fun normalize_adds_scheme_and_trims_slash() {
        assertEquals("https://example.com", CortexUrl.normalizeBaseUrl("example.com"))
        assertEquals("http://example.com", CortexUrl.normalizeBaseUrl("http://example.com/"))
        assertEquals("https://example.com", CortexUrl.normalizeBaseUrl(" https://example.com/ "))
    }

    @Test
    fun build_joins_paths() {
        assertEquals("https://example.com/api/version", CortexUrl.build("https://example.com/", "api/version"))
        assertEquals("https://example.com/api/version", CortexUrl.build("https://example.com", "/api/version"))
    }

    @Test
    fun derive_api_base_prepends_api() {
        assertEquals("https://api.cortex.example.com", CortexUrl.deriveApiBase("https://cortex.example.com"))
        assertEquals("https://api.example.com", CortexUrl.deriveApiBase("https://example.com"))
        // idempotent for already api.*
        assertEquals("https://api.example.com", CortexUrl.deriveApiBase("https://api.example.com"))
    }
}
