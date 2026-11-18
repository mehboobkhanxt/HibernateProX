package com.hibernateprox.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.hibernateprox.app.R

@Composable
fun OverlayDisclosureDialog(
    onGrantPermission: () -> Unit,
    onContinueWithoutOverlay: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Rounded.Layers,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = {
            Text(
                text = stringResource(R.string.overlay_disclosure_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.overlay_disclosure_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            Button(
                onClick = onGrantPermission,
                modifier = Modifier.testTag("overlay_grant_button"),
            ) {
                Text(text = stringResource(R.string.overlay_grant_button))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onContinueWithoutOverlay,
                modifier = Modifier.testTag("overlay_continue_without_button"),
            ) {
                Text(text = stringResource(R.string.overlay_continue_without))
            }
        },
        modifier = Modifier.testTag("overlay_disclosure_dialog"),
    )
}
