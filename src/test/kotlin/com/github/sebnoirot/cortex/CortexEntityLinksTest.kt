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

    @Test
    fun admin_urls_by_type() {
        val base = "https://cortex.example.com"
        assertEquals(
            "https://cortex.example.com/admin/service/en123?catalogPageSlug=services",
            CortexEntityLinks.adminUrl(base, "service", "en123")
        )
        assertEquals(
            "https://cortex.example.com/admin/teams/t1?catalogPageSlug=teams",
            CortexEntityLinks.adminUrl(base, "team", "t1")
        )
        assertEquals(
            "https://cortex.example.com/admin/scorecard/s1?catalogPageSlug=scorecards",
            CortexEntityLinks.adminUrl(base, "scorecard", "s1")
        )
        // unknown type pluralizes with 's'
        assertEquals(
            "https://cortex.example.com/admin/project/p1?catalogPageSlug=projects",
            CortexEntityLinks.adminUrl(base, "project", "p1")
        )
        // group should map to teams UI
        assertEquals(
            "https://cortex.example.com/admin/teams/g1?catalogPageSlug=teams",
            CortexEntityLinks.adminUrl(base, "group", "g1")
        )
    }
}
