package com.github.sebnoirot.cortex.ui

import com.github.sebnoirot.cortex.core.CortexCredentials
import com.github.sebnoirot.cortex.core.CortexHealthClient
import com.github.sebnoirot.cortex.core.CortexSettings
import com.github.sebnoirot.cortex.core.CortexUrl
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.ProjectManager
import com.intellij.ui.EditorNotifications
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*

internal class CortexSettingsConfigurable : Configurable {

    private fun refreshOpenEditors() {
        // Update editor notifications and re-run line markers by restarting daemon across open projects
        ApplicationManager.getApplication().invokeLater {
            ProjectManager.getInstance().openProjects.forEach { project ->
                try {
                    EditorNotifications.getInstance(project).updateAllNotifications()
                } catch (_: Throwable) {
                    // ignore
                }
                try {
                    DaemonCodeAnalyzer.getInstance(project).restart()
                } catch (_: Throwable) {
                    // ignore
                }
            }
        }
    }

    private val baseUrlField = JTextField()
    private val orgSlugField = JTextField()
    private val tokenField = JPasswordField()
    private val statusLabel = JLabel("")
    private val testButton = JButton("Test connection")
    private val createTokenLink = JButton("Create token…")
    private val deleteConfigButton = JButton("Delete configuration…")
    private var rootPanel: JPanel? = null

    override fun getDisplayName(): String = "Cortex"

    override fun createComponent(): JComponent {
        // Constrain preferred widths to avoid horizontal scrollbars; long text scrolls inside the field
        baseUrlField.columns = 40
        orgSlugField.columns = 30
        tokenField.columns = 40
        // Allow fields to shrink horizontally by setting minimal width to 0
        baseUrlField.minimumSize = java.awt.Dimension(0, baseUrlField.preferredSize.height)
        orgSlugField.minimumSize = java.awt.Dimension(0, orgSlugField.preferredSize.height)
        tokenField.minimumSize = java.awt.Dimension(0, tokenField.preferredSize.height)

        val formPanel = JPanel(GridBagLayout())
        val c = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            weightx = 0.0
            insets = Insets(4, 4, 4, 4)
        }

        fun addRow(row: Int, label: String, comp: JComponent) {
            c.gridx = 0; c.gridy = row; c.weightx = 0.0
            formPanel.add(JLabel(label), c)
            c.gridx = 1; c.gridy = row; c.weightx = 1.0
            formPanel.add(comp, c)
        }

        addRow(0, "Base URL:", baseUrlField)
        addRow(1, "Org Slug (optional):", orgSlugField)

        // Token row: field + "Create token…" link/button
        val tokenRow = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(tokenField)
            add(Box.createHorizontalStrut(8))
            add(createTokenLink)
        }
        addRow(2, "Access Token:", tokenRow)

        // Test Row (responsive, avoids horizontal scroll)
        val testPanel = JPanel(GridBagLayout())
        val tc2 = GridBagConstraints().apply {
            insets = Insets(0, 0, 0, 0)
            fill = GridBagConstraints.HORIZONTAL
            weighty = 0.0
        }
        tc2.gridx = 0; tc2.weightx = 0.0
        testPanel.add(testButton, tc2)
        // spacer
        testPanel.add(Box.createHorizontalStrut(8), GridBagConstraints().apply { gridx = 1; weightx = 0.0 })
        // status label expands
        tc2.gridx = 2; tc2.weightx = 1.0
        statusLabel.horizontalAlignment = SwingConstants.LEFT
        statusLabel.minimumSize = java.awt.Dimension(0, statusLabel.preferredSize.height)
        testPanel.add(statusLabel, tc2)
        // delete configuration button on the right
        val tc3 = GridBagConstraints().apply { gridx = 3; weightx = 0.0 }
        testPanel.add(Box.createHorizontalStrut(8), tc3)
        val tc4 = GridBagConstraints().apply { gridx = 4; weightx = 0.0 }
        testPanel.add(deleteConfigButton, tc4)
        addRow(3, "", testPanel)

        // Add a spacer to push content to the top when there is extra vertical space
        c.gridx = 0; c.gridy = 4; c.gridwidth = 2; c.weightx = 1.0; c.weighty = 1.0; c.fill = GridBagConstraints.BOTH
        formPanel.add(Box.createGlue(), c)

        // Open Personal Access Tokens page based on Base URL
        createTokenLink.addActionListener {
            val base = CortexUrl.normalizeBaseUrl(baseUrlField.text)
                ?: service<CortexSettings>().baseUrl?.let { CortexUrl.normalizeBaseUrl(it) }
            if (base == null) {
                statusLabel.text = "Enter a valid Base URL to create a token"
            } else {
                val url = CortexUrl.build(base, "/admin/settings/personal-access-tokens")
                BrowserUtil.browse(url)
            }
        }

        // Delete configuration action
        deleteConfigButton.addActionListener {
            val confirm = JOptionPane.showConfirmDialog(
                rootPanel,
                "Delete Cortex configuration (base URL, API URL, org slug, and token)?",
                "Delete configuration",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
            )
            if (confirm == JOptionPane.OK_OPTION) {
                val settings = service<CortexSettings>()
                settings.baseUrl = null
                settings.apiUrl = null
                settings.orgSlug = null
                service<CortexCredentials>().setToken(null)
                // Reset UI fields without exposing token
                baseUrlField.text = ""
                orgSlugField.text = ""
                tokenField.text = ""
                statusLabel.text = "Configuration deleted"
                                // Refresh editors so cortex.yaml immediately shows the banner again
                                refreshOpenEditors()
            }
        }

        testButton.addActionListener {
            statusLabel.text = "Running…"
            val normalized = CortexUrl.normalizeBaseUrl(baseUrlField.text)
            if (normalized == null) {
                statusLabel.text = "Invalid base URL"
                return@addActionListener
            }
            val apiBase = CortexUrl.deriveApiBase(normalized)
            val typedToken = String(tokenField.password).takeIf { it.isNotBlank() }
            val token = typedToken ?: service<CortexCredentials>().getToken()
            ApplicationManager.getApplication().executeOnPooledThread {
                val result = CortexHealthClient.checkApi(apiBase, token)
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

        val outer = JPanel(BorderLayout())
        outer.add(formPanel, BorderLayout.NORTH)

        rootPanel = outer
        return outer
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
        settings.apiUrl = CortexUrl.deriveApiBase(normalized)
        settings.orgSlug = orgSlugField.text.trim().ifBlank { null }
        val typed = String(tokenField.password)
        if (typed.isNotBlank()) {
            service<CortexCredentials>().setToken(typed)
            // Clear field after saving; do not show plaintext
            tokenField.text = ""
        }
        // Refresh editors so open cortex.yaml updates (banner disappears, links appear)
        refreshOpenEditors()
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
