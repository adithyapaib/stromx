package com.adithyapaib.stromx.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** The five brand colours, plus the few shades derived from them for contrast. */
object Palette {
    val White = Color(0xFFFFFFFF)
    val Silver = Color(0xFFEDEEEB)
    val Heather = Color(0xFFCCC7BF)
    val Night = Color(0xFF31393C)
    val Blue = Color(0xFF2391FF)

    /** Electric blue is too light for small text on white; this shade passes 4.5:1. */
    val BlueText = Color(0xFF1A62B0)

    /** Secondary text on white and silver; heather is too faint to read. */
    val Slate = Color(0xFF5E6669)

    /** Engine at or above [ENGINE_OVERHEAT_C]. */
    val Blood = Color(0xFF8A0303)
    val BloodText = Color(0xFFFFD6D6)
}

/** Engine temperature at which the engine card turns blood red. */
const val ENGINE_OVERHEAT_C = 150

/** Semantic colour roles, one set for light and one for pitch black. */
@Immutable
data class StromXColors(
    val isDark: Boolean,
    val background: Color,
    val card: Color,
    /** Empty gauge cells, tracks and neutral button fills. */
    val fill: Color,
    val segment: Color,
    val segmentSelected: Color,
    val separator: Color,
    val label: Color,
    val labelSecondary: Color,
    val labelTertiary: Color,
    /** Filled gauge cells and bars. */
    val ink: Color,
    val onInk: Color,
    /** Accent for text and icons. */
    val tint: Color,
    /** Accent for filled controls. */
    val tintFill: Color,
    val onTint: Color,
    val tintSoft: Color,
    /** Banners such as "Riding now". */
    val hero: Color,
    val chevron: Color,
    val tabBar: Color,
    val off: Color
)

val LightColors = StromXColors(
    isDark = false,
    background = Palette.Silver,
    card = Palette.White,
    fill = Palette.Heather,
    segment = Color(0xFFDEDFDB),
    segmentSelected = Palette.White,
    separator = Palette.Heather,
    label = Palette.Night,
    labelSecondary = Palette.Night,
    labelTertiary = Palette.Slate,
    ink = Palette.Night,
    onInk = Palette.White,
    tint = Palette.BlueText,
    tintFill = Palette.Blue,
    onTint = Palette.White,
    tintSoft = Color(0xFFE3EFFD),
    hero = Palette.Night,
    chevron = Palette.Heather,
    tabBar = Color(0xF2FFFFFF),
    off = Palette.Heather
)

/** Pitch black for AMOLED: true #000 ground, near-black cards, blue gauges. */
val BlackColors = StromXColors(
    isDark = true,
    background = Color.Black,
    card = Color(0xFF1A1E20),
    fill = Palette.Night,
    segment = Palette.Night,
    segmentSelected = Color(0xFF4A5458),
    separator = Palette.Night,
    label = Palette.White,
    labelSecondary = Palette.Silver,
    labelTertiary = Palette.Heather,
    ink = Palette.Blue,
    onInk = Palette.White,
    tint = Palette.Blue,
    tintFill = Palette.Blue,
    onTint = Palette.White,
    tintSoft = Color(0xFF0E2338),
    hero = Palette.Night,
    chevron = Color(0xFF4A5458),
    tabBar = Color(0xEB000000),
    off = Color(0xFF4A5458)
)

val LocalStromXColors = staticCompositionLocalOf { LightColors }
