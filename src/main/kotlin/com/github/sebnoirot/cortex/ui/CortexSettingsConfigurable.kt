package com.github.sebnoirot.cortex.ui

import com.github.sebnoirot.cortex.core.CortexCredentials
import com.github.sebnoirot.cortex.core.CortexHealthClient
import com.github.sebnoirot.cortex.core.CortexSettings
import com.github.sebnoirot.cortex.core.CortexUrl
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*

internal class CortexSettingsConfigurable : Configurable {

    private val baseUrlField = JTextField()
    private val orgSlugField = JTextField()
    private val tokenField = JPasswordField()
    private val statusLabel = JLabel("")
    private val testButton = JButton("Test connection")
    private var rootPanel: JPanel? = null

    override fun getDisplayName(): String = "Cortex"

    override fun createComponent(): JComponent {
        val panel = JPanel(GridBagLayout())
        val c = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            weightx = 0.0
            insets = Insets(4, 4, 4, 4)
        }

        fun addRow(row: Int, label: String, comp: JComponent) {
            c.gridx = 0; c.gridy = row; c.weightx = 0.0
            panel.add(JLabel(label), c)
            c.gridx = 1; c.gridy = row; c.weightx = 1.0
            panel.add(comp, c)
        }

        addRow(0, "Base URL:", baseUrlField)
        addRow(1, "Org Slug (optional):", orgSlugField)
        addRow(2, "Access Token:", tokenField)

        // Test Row
        val testPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(testButton)
            add(Box.createHorizontalStrut(8))
            add(statusLabel)
        }
        addRow(3, "", testPanel)

        testButton.addActionListener {
            statusLabel.text = "Running…"
            val normalized = CortexUrl.normalizeBaseUrl(baseUrlField.text)
            if (normalized == null) {
                statusLabel.text = "Invalid base URL"
                return@addActionListener
            }
            val typedToken = String(tokenField.password).takeIf { it.isNotBlank() }
            val token = typedToken ?: service<CortexCredentials>().getToken()
            ApplicationManager.getApplication().executeOnPooledThread {
                val result = CortexHealthClient.check(normalized, token)
                val modality = rootPanel?.let { ModalityState.stateForComponent(it) } ?: ModalityState.any()
                ApplicationManager.getApplication().invokeLater({
                    if (rootPanel == null || rootPanel?.isShowing != true) {
                        return@invokeLater
                    }
                    statusLabel.text = if (result.ok) {
                        "OK" + (result.version?.let { " – $it" } ?: "")
                    } else {
                        result.message
                    }
                }, modality)
            }
        }

        rootPanel = panel
        return panel
    }

    override fun isModified(): Boolean {
        val settings = service<CortexSettings>()
        val normalized = CortexUrl.normalizeBaseUrl(baseUrlField.text)
        val baseChanged = (normalized ?: "") != (settings.baseUrl ?: "")
        val orgChanged = orgSlugField.text != (settings.orgSlug ?: "")
        val tokenTyped = tokenField.password.isNotEmpty()
        return baseChanged || orgChanged || tokenTyped
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        val settings = service<CortexSettings>()
        val normalized = CortexUrl.normalizeBaseUrl(baseUrlField.text)
        if (normalized == null) throw ConfigurationException("Invalid Base URL")
        settings.baseUrl = normalized
        settings.orgSlug = orgSlugField.text.trim().ifBlank { null }
        val typed = String(tokenField.password)
        if (typed.isNotBlank()) {
            service<CortexCredentials>().setToken(typed)
            // Clear field after saving; do not show plaintext
            tokenField.text = ""
        }
    }

    override fun reset() {
        val settings = service<CortexSettings>()
        baseUrlField.text = settings.baseUrl ?: ""
        orgSlugField.text = settings.orgSlug ?: ""
        tokenField.text = "" // never show existing token
        statusLabel.text = ""
    }

    override fun disposeUIResources() {
        rootPanel = null
    }
}
