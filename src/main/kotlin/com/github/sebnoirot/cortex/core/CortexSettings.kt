package com.github.sebnoirot.cortex.core

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

/**
 * Application-level settings for Cortex (non-secret fields).
 * Secrets (token) are stored via PasswordSafe – see [CortexCredentials].
 */
@Service(Service.Level.APP)
@State(name = "CortexSettings", storages = [Storage("cortex_settings.xml")])
internal class CortexSettings : PersistentStateComponent<CortexSettings.State> {

    data class State(
        var baseUrl: String? = null,
        var orgSlug: String? = null,
    )

    private var state: State = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    var baseUrl: String?
        get() = state.baseUrl
        set(value) {
            state.baseUrl = value
        }

    var orgSlug: String?
        get() = state.orgSlug
        set(value) {
            state.orgSlug = value
        }
}
