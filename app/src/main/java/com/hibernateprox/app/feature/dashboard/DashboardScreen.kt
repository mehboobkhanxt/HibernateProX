package com.hibernateprox.app.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.hibernateprox.app.core.model.ForceStopOutcome
import com.hibernateprox.app.core.model.StopMode
import com.hibernateprox.app.ui.components.AccessibilityDisclosureDialog
import com.hibernateprox.app.ui.components.OverlayDisclosureDialog

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val neon = LocalNeonColors.current

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshMetrics()
    }

    if (state.showDisclosureDialog) {
        AccessibilityDisclosureDialog(
            onAccept = viewModel::openAccessibilitySettings,
            onDecline = viewModel::dismissDisclosure,
        )
    }

    if (state.showOverlayDialog) {
        OverlayDisclosureDialog(
            onGrantPermission = viewModel::grantOverlayPermission,
            onContinueWithoutOverlay = viewModel::continueWithoutOverlay,
            onDismiss = viewModel::dismissOverlayDialog,
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = stringResource(R.string.dashboard_title),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                NeonStatusPill(
                    label = stringResource(R.string.dashboard_offline_badge),
                    accentColor = neon.emeraldPulse,
                    icon = Icons.Rounded.Shield,
                    iconContentDescription = stringResource(R.string.cd_offline_shield),
                    modifier = Modifier.testTag("offline_badge_pill"),
                )
            }
        }

        item {
            HibernatePulseButton(
                isRunning = state.isBatchRunning,
                onClick = viewModel::onHibernateAllClick,
            )
        }

        state.lastBatchSummary?.let { summary ->
            item {
                NeonGlassCard(
                    accentColor = neon.emeraldPulse,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                        .testTag("last_batch_summary_card"),
                ) {
                    Text(
                        text = stringResource(R.string.batch_results_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(
                            R.string.stop_batch_summary,
                            summary.stoppedCount,
                            summary.alreadyIdleCount,
                            summary.failedCount,
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (summary.items.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            summary.items.take(10).forEach { item ->
                                val ok = item.outcome == ForceStopOutcome.VERIFIED_STOPPED ||
                                    item.outcome == ForceStopOutcome.ALREADY_STOPPED
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = item.appLabel,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    NeonStatusPill(
                                        label = stringResource(
                                            if (ok) {
                                                R.string.app_last_outcome_stopped
                                            } else {
                                                R.string.app_last_outcome_failed
                                            },
                                        ),
                                        accentColor = if (ok) neon.emeraldPulse else neon.crimsonAlert,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            NeonGlassCard(
                accentColor = if (state.isAccessibilityEnabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    neon.amberVolt
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .testTag("foreground_monitor_card"),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.fg_monitor_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Icon(
                        imageVector = Icons.Rounded.Visibility,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                if (!state.isAccessibilityEnabled) {
                    Text(
                        text = stringResource(R.string.fg_monitor_disabled),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = viewModel::openDisclosure,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("dashboard_enable_a11y_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccessibilityNew,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.action_enable_accessibility))
                    }
                } else {
                    val activePkg = state.currentForegroundPackage
                    Text(
                        text = if (activePkg != null) {
                            stringResource(R.string.fg_monitor_current, activePkg)
                        } else {
                            stringResource(R.string.fg_monitor_waiting)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (state.recentTransitions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val recentList = state.recentTransitions.take(3).joinToString(" → ") {
                            it.packageName.substringAfterLast('.')
                        }
                        Text(
                            text = stringResource(R.string.fg_monitor_recent, recentList),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    if (state.canDrawOverlays) {
                        NeonStatusPill(
                            label = stringResource(R.string.overlay_permission_granted),
                            accentColor = neon.emeraldPulse,
                            icon = Icons.Rounded.Layers,
                        )
                    } else {
                        OutlinedButton(
                            onClick = viewModel::grantOverlayPermission,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("dashboard_grant_overlay_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Layers,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.overlay_grant_button))
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MetricGaugeCard(
                    title = stringResource(R.string.dashboard_ram_title),
                    primaryText = stringResource(
                        R.string.dashboard_ram_used_format,
                        state.telemetry.usedRamGb,
                        state.telemetry.totalRamGb,
                    ),
                    secondaryText = stringResource(
                        R.string.dashboard_ram_free_format,
                        state.telemetry.availableRamGb,
                    ),
                    progress = state.telemetry.ramUsageFraction,
                    icon = Icons.Rounded.Memory,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ram_metric_card"),
                )
                MetricGaugeCard(
                    title = stringResource(R.string.dashboard_storage_title),
                    primaryText = stringResource(
                        R.string.dashboard_storage_free_format,
                        state.telemetry.freeStorageGb,
                        state.telemetry.totalStorageGb,
                    ),
                    secondaryText = state.lastRunTimestampMs?.let { ts ->
                        val formatted = java.text.DateFormat.getTimeInstance(
                            java.text.DateFormat.SHORT,
                        ).format(java.util.Date(ts))
                        stringResource(R.string.dashboard_last_run_at, formatted)
                    } ?: stringResource(R.string.dashboard_last_run_never),
                    progress = state.telemetry.storageUsageFraction,
                    icon = Icons.Rounded.Storage,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("storage_metric_card"),
                )
            }
        }

        item {
            val modeAccent = if (state.activeMode == StopMode.ACCESSIBILITY) {
                neon.emeraldPulse
            } else {
                neon.amberVolt
            }
            NeonGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .testTag("active_mode_card"),
                accentColor = modeAccent,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_active_mode_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = stringResource(R.string.cd_mode_status_icon),
                        tint = modeAccent,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(state.activeMode.titleRes),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(state.activeMode.descriptionRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.overlay_honest_note),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun MetricGaugeCard(
    title: String,
    primaryText: String,
    secondaryText: String,
    progress: Float,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    NeonGlassCard(modifier = modifier, accentColor = accentColor) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Icon(imageVector = icon, contentDescription = null, tint = accentColor)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = primaryText,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = secondaryText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = accentColor,
            trackColor = accentColor.copy(alpha = 0.16f),
            strokeCap = StrokeCap.Round,
        )
    }
}
