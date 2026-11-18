package com.hibernateprox.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessibilityNew
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
fun AccessibilityDisclosureDialog(
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDecline,
        icon = {
            Icon(
                imageVector = Icons.Rounded.AccessibilityNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = {
            Text(
                text = stringResource(R.string.disclosure_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.disclosure_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                modifier = Modifier.testTag("disclosure_accept_button"),
            ) {
                Text(text = stringResource(R.string.disclosure_accept))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDecline,
                modifier = Modifier.testTag("disclosure_decline_button"),
            ) {
                Text(text = stringResource(R.string.disclosure_decline))
            }
        },
        modifier = Modifier.testTag("accessibility_disclosure_dialog"),
    )
}
