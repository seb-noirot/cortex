package com.github.sebnoirot.cortex.core

internal object CortexEntityLinks {

    fun serviceUrl(baseUrl: String, id: String): String =
        CortexUrl.build(baseUrl, "/services/$id")

    fun teamUrl(baseUrl: String, id: String): String =
        CortexUrl.build(baseUrl, "/teams/$id")

    fun scorecardUrl(baseUrl: String, id: String): String =
        CortexUrl.build(baseUrl, "/scorecards/$id")

    /**
     * Build admin URL based on entity type and id.
     * Examples:
     *  - service -> /admin/service/{id}?catalogPageSlug=services
     *  - team -> /admin/team/{id}?catalogPageSlug=teams
     *  - scorecard -> /admin/scorecard/{id}?catalogPageSlug=scorecards
     *  - group (alias of team) -> /admin/team/{id}?catalogPageSlug=teams
     */
    fun adminUrl(baseUrl: String, type: String, id: String): String {
        val normalizedType = type.lowercase()
        val pathType = when (normalizedType) {
            "group" -> "teams" // map group to teams UI
            "team" -> "teams"
            else -> normalizedType
        }
        val plural = when (normalizedType) {
            "service" -> "services"
            "team", "group" -> "teams"
            "scorecard" -> "scorecards"
            else -> normalizedType + "s"
        }
        val path = "/admin/${pathType}/${id}?catalogPageSlug=${plural}"
        return CortexUrl.build(baseUrl, path)
    }

    fun adminTeamsUrl(baseUrl: String, id: String): String =
        CortexUrl.build(baseUrl, "/admin/teams/${id}?catalogPageSlug=teams")
}
