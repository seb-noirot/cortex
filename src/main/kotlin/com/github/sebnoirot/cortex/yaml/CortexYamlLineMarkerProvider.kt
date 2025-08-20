package com.github.sebnoirot.cortex.yaml

import com.github.sebnoirot.cortex.core.CortexCredentials
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
import org.jetbrains.yaml.psi.YAMLSequence
import org.jetbrains.yaml.psi.YAMLSequenceItem

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

        // Case 2: x-cortex-tag value
        val isCortexTag = keyValue.keyText == "x-cortex-tag"

        // Case 3: x-cortex-owners item name with provider CORTEX
        var isOwnerNameWithCortexProvider = false
        if (keyValue.keyText == "name") {
            val ownerMapping = keyValue.parent as? YAMLMapping
            val provider = (ownerMapping?.getKeyValueByKey("provider") as? YAMLKeyValue)?.valueText
            if (provider == "CORTEX") {
                val parentItem = ownerMapping.parent
                if (parentItem is YAMLSequenceItem) {
                    val seq = parentItem.parent as? YAMLSequence
                    val ownersKv = seq?.parent as? YAMLKeyValue
                    if (ownersKv?.keyText == "x-cortex-owners") {
                        isOwnerNameWithCortexProvider = true
                    }
                }
            }
        }

        // Case 4: x-cortex-groups scalar value
        val isCortexGroups = keyValue.keyText == "x-cortex-groups"

        // Case 5: x-cortex-dependencies item's tag value
        var isDependencyTag = false
        if (keyValue.keyText == "tag") {
            val mapping = keyValue.parent as? YAMLMapping
            val parentItem = mapping?.parent
            if (parentItem is YAMLSequenceItem) {
                val seq = parentItem.parent as? YAMLSequence
                val depsKv = seq?.parent as? YAMLKeyValue
                if (depsKv?.keyText == "x-cortex-dependencies") {
                    isDependencyTag = true
                }
            }
        }

        if (!isServiceId && !isCortexTag && !isOwnerNameWithCortexProvider && !isCortexGroups && !isDependencyTag) return null

        val valueText = scalar.textValue
        if (valueText.isNullOrBlank()) return null

        val app = ApplicationManager.getApplication()
        val settings = app.getService(CortexSettings::class.java)
        val base = settings.baseUrl?.let { CortexUrl.normalizeBaseUrl(it) } ?: return null

        val infoService = app.getService(CortexEntityInfoService::class.java)

        val tooltipProvider = Function<PsiElement, String> {
            if (isServiceId) {
                val name = infoService.getServiceNameCached(valueText)
                if (!name.isNullOrBlank()) "Open in Cortex: $name" else "Open in Cortex: $valueText"
            } else {
                "Open in Cortex: $valueText"
            }
        }

        val navHandler = GutterIconNavigationHandler<PsiElement> { _event, _elt ->
            if (isServiceId) {
                // Prefetch name asynchronously and open standard service URL
                infoService.ensureServiceFetchedAsync(valueText)
                val url = CortexEntityLinks.serviceUrl(base, valueText)
                BrowserUtil.browse(url)
            } else {
                val apiBase = settings.apiUrl ?: CortexUrl.deriveApiBase(base)
                val creds = app.getService(CortexCredentials::class.java)
                app.executeOnPooledThread {
                    if (isCortexTag || isDependencyTag) {
                        // Resolve via catalog (entity type varies). For dependencies, tag refers to a service tag.
                        val entity = infoService.fetchCatalogEntityByTag(apiBase, creds.getToken(), valueText)
                        if (entity != null) {
                            val adminUrl = CortexEntityLinks.adminUrl(base, entity.type, entity.id)
                            BrowserUtil.browse(adminUrl)
                        } else {
                            val fallback = CortexUrl.build(base, "/admin")
                            BrowserUtil.browse(fallback)
                        }
                    } else {
                        // Owners/groups -> team lookup endpoint
                        val team = infoService.fetchTeamByTagOrId(apiBase, creds.getToken(), valueText)
                        if (team != null) {
                            val adminUrl = CortexEntityLinks.adminTeamsUrl(base, team.id)
                            BrowserUtil.browse(adminUrl)
                        } else {
                            val fallback = CortexUrl.build(base, "/admin")
                            BrowserUtil.browse(fallback)
                        }
                    }
                }
            }
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
