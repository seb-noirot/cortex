package com.github.sebnoirot.cortex

import com.github.sebnoirot.cortex.core.CortexDetection
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class CortexDetectionTest : BasePlatformTestCase() {

    fun testDetectionWhenFilePresent() {
        myFixture.addFileToProject("cortex.yaml", "name: demo")
        assertTrue(CortexDetection.hasCortexYaml(project))
    }

    fun testDetectionWhenFileAbsent() {
        assertFalse(CortexDetection.hasCortexYaml(project))
    }
}
