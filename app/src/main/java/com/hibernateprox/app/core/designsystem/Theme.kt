package com.hibernateprox.app.core.designsystem

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Immutable
data class NeonExtColors(
    val glassFill: Color = ObsidianGlass,
    val glassBorder: Color = ObsidianBorder,
    val neonCyan: Color = NeonCyan,
    val neonViolet: Color = NeonViolet,
    val emeraldPulse: Color = EmeraldPulse,
    val amberVolt: Color = AmberVolt,
    val crimsonAlert: Color = CrimsonAlert,
)

val LocalNeonColors = staticCompositionLocalOf { NeonExtColors() }

private val AmoledNeonDarkScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = AmoledVoid,
    primaryContainer = NeonCyanSoft,
    onPrimaryContainer = IceWhite,
    secondary = NeonViolet,
    onSecondary = IceWhite,
    secondaryContainer = NeonVioletSoft,
    onSecondaryContainer = IceWhite,
    tertiary = EmeraldPulse,
    onTertiary = AmoledVoid,
    background = AmoledVoid,
    onBackground = IceWhite,
    surface = ObsidianSurface,
    onSurface = IceWhite,
    surfaceVariant = ObsidianElevated,
    onSurfaceVariant = CoolSlate,
    outline = ObsidianBorder,
    error = CrimsonAlert,
    onError = IceWhite,
)

@Composable
fun HibernateProTheme(
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val baseScheme =
        if (useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val dynamic = dynamicDarkColorScheme(LocalContext.current)
            dynamic.copy(
                background = AmoledVoid,
                surface = ObsidianSurface,
                surfaceVariant = ObsidianElevated,
            )
        } else {
            AmoledNeonDarkScheme
        }

    val neonExt = NeonExtColors(
        neonCyan = baseScheme.primary,
        neonViolet = baseScheme.secondary,
    )

    CompositionLocalProvider(LocalNeonColors provides neonExt) {
        MaterialTheme(
            colorScheme = baseScheme,
            typography = HibernateTypography,
            shapes = HibernateShapes,
            content = content,
        )
    }
}
