package com.hibernateprox.app.feature.dashboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hibernateprox.app.R
import com.hibernateprox.app.core.designsystem.LocalNeonColors

@Composable
fun HibernatePulseButton(
    isRunning: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val neon = LocalNeonColors.current
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val cdText = stringResource(R.string.cd_hibernate_button)

    val infiniteTransition = rememberInfiniteTransition(label = "neon_ring")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 0.86f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_radius",
    )
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isRunning) 900 else 3600, easing = LinearEasing),
        ),
        label = "sweep_angle",
    )
    val buttonScale by animateFloatAsState(
        targetValue = if (isRunning) 0.94f else 1f,
        animationSpec = spring(),
        label = "btn_scale",
    )

    Box(
        modifier = modifier.size(196.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val outerRadius = (size.minDimension / 2f) * pulseRadius
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primary.copy(alpha = 0.24f), primary.copy(alpha = 0f)),
                ),
                radius = outerRadius,
            )
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(primary, secondary, primary.copy(alpha = 0.2f), primary),
                ),
                startAngle = sweepAngle,
                sweepAngle = if (isRunning) 260f else 140f,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
            )
        }

        Column(
            modifier = Modifier
                .size(152.dp)
                .scale(buttonScale)
                .minimumInteractiveComponentSize()
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surface,
                        ),
                    ),
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = primary),
                    role = Role.Button,
                ) {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
                .semantics { contentDescription = cdText }
                .testTag("hibernate_all_button"),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Rounded.PowerSettingsNew,
                contentDescription = null,
                tint = if (isRunning) neon.emeraldPulse else primary,
                modifier = Modifier.size(38.dp),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(
                    if (isRunning) R.string.dashboard_hibernating else R.string.dashboard_hibernate_all,
                ),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
