package com.github.sebnoirot.cortex.core

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.generateServiceName
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.ide.passwordSafe.PasswordSafe

/**
 * Access token storage using PasswordSafe. Never log or expose the token.
 */
@Service(Service.Level.APP)
internal class CortexCredentials {

    private val log = Logger.getInstance(CortexCredentials::class.java)

    private val attributes: CredentialAttributes = CredentialAttributes(
        generateServiceName("Cortex", "Access Token")
    )

    fun getToken(): String? = try {
        PasswordSafe.instance.getPassword(attributes)
    } catch (t: Throwable) {
        log.warn("Failed to read Cortex token from PasswordSafe: ${t.message}")
        null
    }

    fun setToken(token: String?) {
        try {
            if (token.isNullOrBlank()) {
                PasswordSafe.instance.setPassword(attributes, null)
            } else {
                PasswordSafe.instance.setPassword(attributes, token)
            }
        } catch (t: Throwable) {
            log.warn("Failed to write Cortex token to PasswordSafe: ${redact(token)}: ${t.message}")
        }
    }

    fun redact(value: String?): String = if (value.isNullOrEmpty()) "" else "***"
}
