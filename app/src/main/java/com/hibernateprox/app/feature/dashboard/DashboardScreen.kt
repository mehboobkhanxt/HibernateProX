package com.hibernateprox.app.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hibernateprox.app.R
import com.hibernateprox.app.core.designsystem.LocalNeonColors
import com.hibernateprox.app.core.designsystem.NeonGlassCard
import com.hibernateprox.app.core.designsystem.NeonStatusPill

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val neon = LocalNeonColors.current

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
                isRunning = state.isPreviewPulsing,
                onClick = viewModel::triggerPulsePreview,
            )
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
                    secondaryText = stringResource(R.string.dashboard_last_run_never),
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
            NeonGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .testTag("active_mode_card"),
                accentColor = neon.amberVolt,
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
                        tint = neon.amberVolt,
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
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.dashboard_phase1_notice),
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
    accentColor: androidx.compose.ui.graphics.Color,
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
