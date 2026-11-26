package com.adithyapaib.stromx.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.adithyapaib.stromx.settings.Appearance

private val LocalStromXType = staticCompositionLocalOf { StromXType() }

/** Access to the app's colour roles and type scale: `StromX.colors.card`, `StromX.type.body`. */
object StromX {
    val colors: StromXColors
        @Composable @ReadOnlyComposable get() = LocalStromXColors.current
    val type: StromXType
        @Composable @ReadOnlyComposable get() = LocalStromXType.current
}

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/** True when [appearance] resolves to the pitch-black scheme right now. */
@Composable
fun Appearance.isDark(): Boolean = when (this) {
    Appearance.AUTO -> isSystemInDarkTheme()
    Appearance.LIGHT -> false
    Appearance.BLACK -> true
}

@Composable
fun StromXTheme(appearance: Appearance = Appearance.AUTO, content: @Composable () -> Unit) {
    val dark = appearance.isDark()
    val colors = if (dark) BlackColors else LightColors
    val scheme = if (dark) {
        darkColorScheme(
            primary = colors.tintFill,
            onPrimary = colors.onTint,
            primaryContainer = colors.tintSoft,
            onPrimaryContainer = colors.tint,
            secondary = colors.tintFill,
            onSecondary = colors.onTint,
            background = colors.background,
            onBackground = colors.label,
            surface = colors.card,
            onSurface = colors.label,
            surfaceVariant = colors.fill,
            onSurfaceVariant = colors.labelTertiary,
            surfaceContainerLowest = colors.background,
            surfaceContainerLow = colors.card,
            surfaceContainer = colors.card,
            surfaceContainerHigh = colors.card,
            surfaceContainerHighest = colors.fill,
            outline = colors.separator,
            outlineVariant = colors.separator,
            error = Palette.BloodText,
            onError = Palette.Blood,
            inverseSurface = Palette.Silver,
            inverseOnSurface = Palette.Night,
            inversePrimary = Palette.BlueText
        )
    } else {
        lightColorScheme(
            primary = colors.tintFill,
            onPrimary = colors.onTint,
            primaryContainer = colors.tintSoft,
            onPrimaryContainer = colors.tint,
            secondary = colors.tintFill,
            onSecondary = colors.onTint,
            background = colors.background,
            onBackground = colors.label,
            surface = colors.card,
            onSurface = colors.label,
            surfaceVariant = colors.background,
            onSurfaceVariant = colors.labelTertiary,
            surfaceContainerLowest = colors.card,
            surfaceContainerLow = colors.card,
            surfaceContainer = colors.card,
            surfaceContainerHigh = colors.card,
            surfaceContainerHighest = colors.background,
            outline = colors.separator,
            outlineVariant = colors.separator,
            error = Palette.Blood,
            onError = Palette.White,
            inverseSurface = Palette.Night,
            inverseOnSurface = Palette.White,
            inversePrimary = Palette.Blue
        )
    }

    // Status and navigation bar icons follow the scheme, not the system setting.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }

    MaterialTheme(colorScheme = scheme, typography = MaterialTypography, shapes = AppShapes) {
        CompositionLocalProvider(
            LocalStromXColors provides colors,
            LocalStromXType provides StromXType(),
            LocalTextSelectionColors provides TextSelectionColors(
                handleColor = colors.tintFill,
                backgroundColor = colors.tintFill.copy(alpha = 0.3f)
            ),
            content = content
        )
    }
}
