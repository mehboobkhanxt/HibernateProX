package com.hibernateprox.app.feature.settings

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hibernateprox.app.R
import com.hibernateprox.app.core.designsystem.LocalNeonColors
import com.hibernateprox.app.core.designsystem.NeonGlassCard
import com.hibernateprox.app.core.designsystem.NeonStatusPill
import com.hibernateprox.app.core.model.StopMode

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val useDynamicColor by viewModel.useDynamicColor.collectAsStateWithLifecycle()
    val neon = LocalNeonColors.current
    val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Text(
                text = stringResource(R.string.settings_header),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
            )
        }

        item {
            NeonGlassCard(
                accentColor = neon.emeraldPulse,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .testTag("offline_privacy_card"),
            ) {
                NeonStatusPill(
                    label = stringResource(R.string.dashboard_offline_badge),
                    accentColor = neon.emeraldPulse,
                    icon = Icons.Rounded.Lock,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.settings_offline_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.settings_offline_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            NeonGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_dynamic_color),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(
                                if (supportsDynamicColor) {
                                    R.string.settings_dynamic_color_desc
                                } else {
                                    R.string.settings_theme_badge
                                },
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = useDynamicColor && supportsDynamicColor,
                        onCheckedChange = viewModel::toggleDynamicColor,
                        enabled = supportsDynamicColor,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("dynamic_color_switch"),
                    )
                }
            }
        }

        items(
            items = listOf(StopMode.SHIZUKU, StopMode.ROOT, StopMode.ACCESSIBILITY),
            key = { it.name },
        ) { mode ->
            NeonGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
                accentColor = MaterialTheme.colorScheme.secondary,
            ) {
                Text(
                    text = stringResource(mode.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(mode.descriptionRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
