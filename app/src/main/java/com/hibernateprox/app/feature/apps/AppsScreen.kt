package com.hibernateprox.app.feature.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hibernateprox.app.R
import com.hibernateprox.app.core.designsystem.LocalNeonColors
import com.hibernateprox.app.core.designsystem.NeonGlassCard
import com.hibernateprox.app.core.designsystem.NeonStatusPill
import com.hibernateprox.app.core.model.AppRuleMode
import com.hibernateprox.app.core.model.ForceStopOutcome
import com.hibernateprox.app.core.model.ForceStopState
import com.hibernateprox.app.core.model.InstalledAppInfo
import com.hibernateprox.app.ui.components.AccessibilityDisclosureDialog

@Composable
fun AppsScreen(
    modifier: Modifier = Modifier,
    viewModel: AppsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val neon = LocalNeonColors.current

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
    }

    if (state.showDisclosureDialog) {
        AccessibilityDisclosureDialog(
            onAccept = viewModel::openAccessibilitySettings,
            onDecline = viewModel::dismissDisclosure,
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("apps_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.apps_header),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                IconButton(
                    onClick = viewModel::refresh,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("apps_refresh_button"),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = stringResource(R.string.action_refresh),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        if (!state.isAccessibilityEnabled) {
            item {
                NeonGlassCard(
                    accentColor = neon.amberVolt,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                        .testTag("apps_enable_a11y_card"),
                ) {
                    Text(
                        text = stringResource(R.string.stop_result_need_a11y),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = viewModel::openDisclosure,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("apps_enable_a11y_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccessibilityNew,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.action_enable_accessibility))
                    }
                }
            }
        }

        item {
            ForceStopProgressBanner(
                stopState = state.stopState,
                onCancel = viewModel::cancelActiveStop,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
            )
        }

        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::setSearchQuery,
                singleLine = true,
                leadingIcon = {
                    Icon(imageVector = Icons.Rounded.Search, contentDescription = null)
                },
                placeholder = { Text(stringResource(R.string.apps_search_hint)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .testTag("apps_search_input"),
            )
        }

        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(AppsFilter.entries, key = { it.name }) { entry ->
                    val labelRes = when (entry) {
                        AppsFilter.ALL -> R.string.apps_filter_all
                        AppsFilter.ACTIVE -> R.string.apps_filter_active
                        AppsFilter.USER -> R.string.apps_filter_user
                        AppsFilter.SYSTEM -> R.string.apps_filter_system
                        AppsFilter.SAFE_LIST -> R.string.apps_filter_safelist
                        AppsFilter.WHITELISTED -> R.string.apps_filter_whitelisted
                        AppsFilter.BLACKLISTED -> R.string.apps_filter_blacklisted
                    }
                    FilterChip(
                        selected = state.filter == entry,
                        onClick = { viewModel.setFilter(entry) },
                        label = { Text(stringResource(labelRes)) },
                        modifier = Modifier.testTag("apps_filter_${entry.name.lowercase()}"),
                    )
                }
            }
        }

        if (state.isLoading) {
            item {
                CircularProgressIndicator(modifier = Modifier.padding(24.dp))
            }
        } else if (state.apps.isEmpty()) {
            item {
                NeonGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp),
                ) {
                    Text(
                        text = stringResource(R.string.apps_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(state.apps, key = { it.packageName }) { app ->
                AppPackageRow(
                    app = app,
                    onRuleSelected = { mode -> viewModel.setAppRule(app, mode) },
                    onForceStop = { viewModel.forceStopApp(app) },
                    onOpenAppInfo = { viewModel.openAppInfoManually(app.packageName) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp),
                )
            }
        }
    }
}

@Composable
private fun ForceStopProgressBanner(
    stopState: ForceStopState,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val neon = LocalNeonColors.current
    val message = when (stopState) {
        ForceStopState.Idle -> return
        is ForceStopState.OpeningAppInfo ->
            stringResource(R.string.stop_status_opening, stopState.appLabel)
        is ForceStopState.ClickingForceStop ->
            stringResource(R.string.stop_status_clicking, stopState.appLabel)
        is ForceStopState.ConfirmingDialog ->
            stringResource(R.string.stop_status_confirming, stopState.appLabel)
        is ForceStopState.Verifying ->
            stringResource(R.string.stop_status_verifying, stopState.appLabel)
        is ForceStopState.Completed -> when (stopState.outcome) {
            ForceStopOutcome.VERIFIED_STOPPED ->
                stringResource(R.string.stop_result_verified, stopState.appLabel)
            ForceStopOutcome.ALREADY_STOPPED ->
                stringResource(R.string.stop_result_already_stopped, stopState.appLabel)
            ForceStopOutcome.BUTTON_NOT_FOUND ->
                stringResource(R.string.stop_result_not_found, stopState.appLabel)
            ForceStopOutcome.TIMED_OUT ->
                stringResource(R.string.stop_result_timeout, stopState.appLabel)
            ForceStopOutcome.PROTECTED_SKIPPED ->
                stringResource(R.string.apps_badge_protected)
            ForceStopOutcome.ACCESSIBILITY_DISABLED ->
                stringResource(R.string.stop_result_need_a11y)
        }
    }

    val isActive = stopState !is ForceStopState.Completed
    val accent = when {
        stopState is ForceStopState.Completed &&
            stopState.outcome == ForceStopOutcome.VERIFIED_STOPPED -> neon.emeraldPulse
        stopState is ForceStopState.Completed &&
            (stopState.outcome == ForceStopOutcome.TIMED_OUT ||
                stopState.outcome == ForceStopOutcome.BUTTON_NOT_FOUND) -> neon.crimsonAlert
        else -> MaterialTheme.colorScheme.primary
    }

    NeonGlassCard(accentColor = accent, modifier = modifier.testTag("force_stop_status_banner")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            if (isActive) {
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.testTag("cancel_force_stop_button"),
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        }
    }
}

@Composable
private fun AppPackageRow(
    app: InstalledAppInfo,
    onRuleSelected: (AppRuleMode) -> Unit,
    onForceStop: () -> Unit,
    onOpenAppInfo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val neon = LocalNeonColors.current
    val badgeColor = when {
        app.isProtectedSafeList -> neon.amberVolt
        app.isStopped -> neon.emeraldPulse
        else -> MaterialTheme.colorScheme.primary
    }
    val badgeText = when {
        app.isProtectedSafeList -> stringResource(R.string.apps_badge_protected)
        app.isStopped -> stringResource(R.string.apps_badge_stopped)
        else -> stringResource(R.string.apps_badge_running)
    }

    NeonGlassCard(
        accentColor = if (app.isForegroundNow) neon.neonViolet else badgeColor,
        modifier = modifier.testTag("app_row_${app.packageName}"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${if (app.isSystemApp) stringResource(R.string.apps_system_tag) else stringResource(R.string.apps_user_tag)} · ${app.packageName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            NeonStatusPill(
                label = badgeText,
                accentColor = badgeColor,
                icon = if (app.isProtectedSafeList) Icons.Rounded.Lock else null,
                iconContentDescription = if (app.isProtectedSafeList) {
                    stringResource(R.string.cd_protected_lock)
                } else {
                    null
                },
            )
        }

        if (!app.isProtectedSafeList) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppRuleMode.entries.forEach { mode ->
                    val labelRes = when (mode) {
                        AppRuleMode.DEFAULT -> R.string.rule_default
                        AppRuleMode.WHITELIST -> R.string.rule_whitelist
                        AppRuleMode.BLACKLIST -> R.string.rule_blacklist
                    }
                    FilterChip(
                        selected = app.ruleMode == mode,
                        onClick = { onRuleSelected(mode) },
                        label = { Text(stringResource(labelRes)) },
                        modifier = Modifier.testTag("rule_${mode.name.lowercase()}_${app.packageName}"),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = onOpenAppInfo,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("app_info_btn_${app.packageName}"),
            ) {
                Icon(
                    imageVector = Icons.Rounded.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.action_open_app_info))
            }

            if (!app.isProtectedSafeList) {
                Button(
                    onClick = onForceStop,
                    enabled = !app.isStopped && app.ruleMode != AppRuleMode.WHITELIST,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("force_stop_btn_${app.packageName}"),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PowerSettingsNew,
                        contentDescription = stringResource(R.string.cd_force_stop_app, app.label),
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.action_force_stop))
                }
            }
        }
    }
}
