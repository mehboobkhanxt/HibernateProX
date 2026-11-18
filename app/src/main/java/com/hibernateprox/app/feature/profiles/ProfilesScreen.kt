package com.hibernateprox.app.feature.profiles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hibernateprox.app.R
import com.hibernateprox.app.core.designsystem.LocalNeonColors
import com.hibernateprox.app.core.designsystem.NeonGlassCard
import com.hibernateprox.app.core.model.ProfilePreset

@Composable
fun ProfilesScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfilesViewModel = hiltViewModel(),
) {
    val selectedKey by viewModel.selectedProfileKey.collectAsStateWithLifecycle()
    val neon = LocalNeonColors.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profiles_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Text(
                text = stringResource(R.string.profiles_header),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.profiles_phase_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
            )
        }

        items(ProfilePreset.entries, key = { it.key }) { preset ->
            val isSelected = preset.key == selectedKey
            val accent = if (isSelected) neon.emeraldPulse else MaterialTheme.colorScheme.primary
            NeonGlassCard(
                accentColor = accent,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .clip(MaterialTheme.shapes.large)
                    .minimumInteractiveComponentSize()
                    .clickable { viewModel.selectProfile(preset.key) }
                    .testTag("profile_card_${preset.key}"),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(preset.titleRes),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Icon(
                        imageVector = if (isSelected) {
                            Icons.Rounded.CheckCircle
                        } else {
                            Icons.Rounded.RadioButtonUnchecked
                        },
                        contentDescription = null,
                        tint = accent,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(preset.descriptionRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
