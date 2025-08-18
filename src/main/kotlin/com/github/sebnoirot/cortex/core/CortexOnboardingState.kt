package com.github.sebnoirot.cortex.core

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.Service

@Service(Service.Level.PROJECT)
@State(name = "CortexOnboardingState", storages = [Storage("cortex_settings.xml")])
internal class CortexOnboardingState() : PersistentStateComponent<CortexOnboardingState.State> {

    data class State(
        var onboardingShown: Boolean = false
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    var isOnboardingShown: Boolean
        get() = state.onboardingShown
        set(value) {
            state.onboardingShown = value
        }
}
