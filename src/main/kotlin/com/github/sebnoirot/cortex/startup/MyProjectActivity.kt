package com.github.sebnoirot.cortex.startup

import com.github.sebnoirot.cortex.core.*
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.ide.BrowserUtil

class MyProjectActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        val state = project.service<CortexOnboardingState>()
        if (state.isOnboardingShown) return

        ApplicationManager.getApplication().executeOnPooledThread {
            val hasCortex = try {
                CortexDetection.hasCortexYaml(project)
            } catch (t: Throwable) {
                thisLogger().warn("Cortex detection failed: ${t.message}")
                false
            }

            if (hasCortex) {
                // Try to resolve x-cortex-tag and propose to open Cortex project if available
                val settings = service<CortexSettings>()
                val base = settings.baseUrl?.let { CortexUrl.normalizeBaseUrl(it) }
                val apiBase = settings.apiUrl
                val creds = service<CortexCredentials>()
                val tag = try { CortexDetection.findCortexTag(project) } catch (_: Throwable) { null }

                var openUrl: String? = null
                if (!tag.isNullOrBlank() && !apiBase.isNullOrBlank() && !base.isNullOrBlank()) {
                    val info = service<CortexEntityInfoService>()
                    val entity = info.fetchCatalogEntityByTag(apiBase, creds.getToken(), tag)
                    if (entity != null) {
                        openUrl = CortexEntityLinks.adminUrl(base, entity.type, entity.id)
                    }
                }

                state.isOnboardingShown = true
                ApplicationManager.getApplication().invokeLater {
                    val group = NotificationGroupManager.getInstance().getNotificationGroup("Cortex Notifications")
                    val content = if (openUrl != null) {
                        "Detected cortex.yaml with tag '$tag'."
                    } else {
                        "Detected cortex.yaml. Configure Cortex endpoint and token in Settings › Tools › Cortex."
                    }
                    val notification = group.createNotification(
                        "Cortex configuration",
                        content,
                        NotificationType.INFORMATION
                    )
                    if (openUrl != null) {
                        notification.addAction(NotificationAction.createSimple("Open in Cortex") {
                            BrowserUtil.browse(openUrl!!)
                        })
                    }
                    notification.notify(project)
                }
            }
        }
    }
}