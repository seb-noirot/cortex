package com.github.sebnoirot.cortex

import com.github.sebnoirot.cortex.core.CortexEntityLinks
import org.junit.Assert.assertEquals
import org.junit.Test

class CortexEntityLinksTest {

    @Test
    fun service_team_scorecard_urls() {
        val base = "https://example.com/"
        assertEquals("https://example.com/services/svc-1", CortexEntityLinks.serviceUrl(base, "svc-1"))
        assertEquals("https://example.com/teams/team-2", CortexEntityLinks.teamUrl(base, "team-2"))
        assertEquals("https://example.com/scorecards/sc-3", CortexEntityLinks.scorecardUrl(base, "sc-3"))
    }
}
