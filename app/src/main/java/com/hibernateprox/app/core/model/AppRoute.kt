package com.hibernateprox.app.core.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute {
    @Serializable data object Dashboard : AppRoute
    @Serializable data object Apps : AppRoute
    @Serializable data object Profiles : AppRoute
    @Serializable data object Automation : AppRoute
    @Serializable data object Stats : AppRoute
    @Serializable data object Settings : AppRoute
}
