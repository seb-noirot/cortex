package com.github.sebnoirot.cortex.core

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping

/**
 * Simple helper to detect presence of a cortex.yaml file in the project.
 * Uses indices via FilenameIndex under a read action.
 */
internal object CortexDetection {
    fun hasCortexYaml(project: Project): Boolean {
        return ReadAction.compute<Boolean, RuntimeException> {
            val files = FilenameIndex.getVirtualFilesByName(
                "cortex.yaml",
                true,
                GlobalSearchScope.projectScope(project)
            ).toImmutableList()
            files.isNotEmpty()
        }
    }

    /**
     * Attempts to read x-cortex-tag from cortex.yaml (under info mapping if present), returns the scalar value or null.
     */
    fun findCortexTag(project: Project): String? {
        return ReadAction.compute<String?, RuntimeException> {
            val vfiles = FilenameIndex.getVirtualFilesByName(
                "cortex.yaml",
                true,
                GlobalSearchScope.projectScope(project)
            )
            val psiManager = PsiManager.getInstance(project)
            vfiles.forEach { vf ->
                val psi = psiManager.findFile(vf) as? YAMLFile ?: return@forEach
                val doc = psi.documents.firstOrNull() ?: return@forEach
                val top = doc.topLevelValue as? YAMLMapping ?: return@forEach
                // Check info -> x-cortex-tag
                val info = top.getKeyValueByKey("info")?.value as? YAMLMapping
                val tagUnderInfo = (info?.getKeyValueByKey("x-cortex-tag") as? YAMLKeyValue)?.valueText
                if (!tagUnderInfo.isNullOrBlank()) return@compute tagUnderInfo
                // Fallback: at top-level
                val topTag = top.getKeyValueByKey("x-cortex-tag")?.valueText
                if (!topTag.isNullOrBlank()) return@compute topTag
            }
            null
        }
    }
}
