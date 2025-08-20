package com.github.sebnoirot.cortex.yaml

import com.github.sebnoirot.cortex.core.CortexSettings
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import java.util.function.Function
import javax.swing.JComponent

/**
 * Shows a lightweight editor notification on cortex.yaml when Cortex is not configured yet.
 * Provides a direct link to open Settings  Tools  Cortex.
 */
internal class CortexYamlEditorNotificationProvider : EditorNotificationProvider {
    override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?>? {
        // Only for cortex.yaml
        if (!file.isValid || file.isDirectory) return null
        if (!file.name.equals("cortex.yaml", ignoreCase = true)) return null

        val settings = service<CortexSettings>()
        val needsConfig = settings.baseUrl.isNullOrBlank()
        if (!needsConfig) return null

        return Function { editor ->
            // Only show on text editors
            if (editor !is TextEditor) return@Function null
            EditorNotificationPanel(EditorNotificationPanel.Status.Info).apply {
                text = "Cortex is not configured. Configure the Cortex domain and token to enable navigation."
                createActionLabel("Configure Cortex…") {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, "Cortex")
                }
            }
        }
    }
}
