package com.github.sebnoirot.cortex.startup

import com.github.sebnoirot.cortex.core.CortexDetection
import com.github.sebnoirot.cortex.core.CortexOnboardingState
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

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
                state.isOnboardingShown = true
                ApplicationManager.getApplication().invokeLater {
                    val group = NotificationGroupManager.getInstance().getNotificationGroup("Cortex Notifications")
                    val notification = group.createNotification(
                        "Cortex configuration",
                        "Detected cortex.yaml. Configure Cortex endpoint and token in Settings › Tools › Cortex.",
                        NotificationType.INFORMATION
                    )
                    notification.notify(project)
                }
            }
        }
    }
}