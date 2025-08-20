package com.github.sebnoirot.cortex.yaml

import com.github.sebnoirot.cortex.core.CortexEntityInfoService
import com.github.sebnoirot.cortex.core.CortexEntityLinks
import com.github.sebnoirot.cortex.core.CortexSettings
import com.github.sebnoirot.cortex.core.CortexUrl
import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.util.Function
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLScalar

internal class CortexYamlLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val scalar = element as? YAMLScalar ?: return null
        val keyValue = scalar.parent as? YAMLKeyValue ?: return null

        // Case 1: service.id under service mapping
        var isServiceId = false
        if (keyValue.keyText == "id") {
            val parentMapping = keyValue.parent as? YAMLMapping
            val parentKv = parentMapping?.parent as? YAMLKeyValue
            if (parentKv?.keyText == "service") {
                isServiceId = true
            }
        }

        // Case 2: x-cortex-tag at top or any mapping level where value is a scalar service id
        val isCortexTag = keyValue.keyText == "x-cortex-tag"

        if (!isServiceId && !isCortexTag) return null

        val id = scalar.textValue
        if (id.isNullOrBlank()) return null

        val app = ApplicationManager.getApplication()
        val settings = app.getService(CortexSettings::class.java)
        val base = settings.baseUrl?.let { CortexUrl.normalizeBaseUrl(it) } ?: return null

        val infoService = app.getService(CortexEntityInfoService::class.java)
        infoService.ensureServiceFetchedAsync(id)

        val tooltipProvider = Function<PsiElement, String> {
            val name = infoService.getServiceNameCached(id)
            if (!name.isNullOrBlank()) "Open in Cortex: $name" else "Open in Cortex: $id"
        }

        val navHandler = GutterIconNavigationHandler<PsiElement> { _event, _elt ->
            val url = CortexEntityLinks.serviceUrl(base, id)
            BrowserUtil.browse(url)
        }

        return LineMarkerInfo(
            scalar,
            scalar.textRange,
            AllIcons.General.Web,
            tooltipProvider,
            navHandler,
            GutterIconRenderer.Alignment.LEFT,
            { "Open in Cortex" }
        )
    }
}
