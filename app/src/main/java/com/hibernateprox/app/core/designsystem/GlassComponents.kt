package com.hibernateprox.app.core.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun AmoledAtmosphereBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val primaryGlow = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
    val secondaryGlow = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
    val bg = MaterialTheme.colorScheme.background
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryGlow, Color.Transparent),
                        center = Offset(size.width * 0.15f, size.height * 0.08f),
                        radius = size.maxDimension * 0.55f,
                    ),
                    radius = size.maxDimension * 0.55f,
                    center = Offset(size.width * 0.15f, size.height * 0.08f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(secondaryGlow, Color.Transparent),
                        center = Offset(size.width * 0.88f, size.height * 0.42f),
                        radius = size.maxDimension * 0.48f,
                    ),
                    radius = size.maxDimension * 0.48f,
                    center = Offset(size.width * 0.88f, size.height * 0.42f),
                )
            },
        content = content,
    )
}

@Composable
fun NeonGlassCard(
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable ColumnScope.() -> Unit,
) {
    val neon = LocalNeonColors.current
    val borderBrush = Brush.linearGradient(
        colors = listOf(
            accentColor.copy(alpha = 0.45f),
            neon.glassBorder,
            neon.neonViolet.copy(alpha = 0.25f),
        ),
    )
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = neon.glassFill,
        border = BorderStroke(1.dp, borderBrush),
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun NeonStatusPill(
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconContentDescription: String? = null,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(accentColor.copy(alpha = 0.14f))
            .border(1.dp, accentColor.copy(alpha = 0.45f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = iconContentDescription,
                tint = accentColor,
                modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = accentColor,
        )
    }
}
