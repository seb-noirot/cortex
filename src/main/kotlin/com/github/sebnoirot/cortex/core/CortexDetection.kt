package com.github.sebnoirot.cortex.core

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import kotlinx.collections.immutable.toImmutableList

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
}
