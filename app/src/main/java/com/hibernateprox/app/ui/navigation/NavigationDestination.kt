package com.hibernateprox.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.AutoMode
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AutoMode
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.ui.graphics.vector.ImageVector
import com.hibernateprox.app.R
import com.hibernateprox.app.core.model.AppRoute

data class TopLevelDestination(
    val route: AppRoute,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
)

val topLevelDestinations = listOf(
    TopLevelDestination(
        route = AppRoute.Dashboard,
        labelRes = R.string.nav_dashboard,
        selectedIcon = Icons.Rounded.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard,
        testTag = "nav_item_dashboard",
    ),
    TopLevelDestination(
        route = AppRoute.Apps,
        labelRes = R.string.nav_apps,
        selectedIcon = Icons.Rounded.Apps,
        unselectedIcon = Icons.Outlined.Apps,
        testTag = "nav_item_apps",
    ),
    TopLevelDestination(
        route = AppRoute.Profiles,
        labelRes = R.string.nav_profiles,
        selectedIcon = Icons.Rounded.Tune,
        unselectedIcon = Icons.Outlined.Tune,
        testTag = "nav_item_profiles",
    ),
    TopLevelDestination(
        route = AppRoute.Automation,
        labelRes = R.string.nav_automation,
        selectedIcon = Icons.Rounded.AutoMode,
        unselectedIcon = Icons.Outlined.AutoMode,
        testTag = "nav_item_automation",
    ),
    TopLevelDestination(
        route = AppRoute.Stats,
        labelRes = R.string.nav_stats,
        selectedIcon = Icons.Rounded.QueryStats,
        unselectedIcon = Icons.Outlined.QueryStats,
        testTag = "nav_item_stats",
    ),
    TopLevelDestination(
        route = AppRoute.Settings,
        labelRes = R.string.nav_settings,
        selectedIcon = Icons.Rounded.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        testTag = "nav_item_settings",
    ),
)
