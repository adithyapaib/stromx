package com.adithyapaib.stromx.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.adithyapaib.stromx.R

@OptIn(ExperimentalTextApi::class)
private fun geist(weight: FontWeight) = Font(
    resId = R.font.geist,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight))
)

/** Geist, bundled as one variable font (SIL OFL, see licenses/Geist-OFL.txt). */
val Geist = FontFamily(
    geist(FontWeight.ExtraLight),
    geist(FontWeight.Light),
    geist(FontWeight.Normal),
    geist(FontWeight.Medium),
    geist(FontWeight.SemiBold),
    geist(FontWeight.Bold),
    geist(FontWeight.ExtraBold)
)

private const val TABULAR = "tnum"

private fun style(size: Int, line: Int, weight: FontWeight, tracking: Double = 0.0, tabular: Boolean = false) = TextStyle(
    fontFamily = Geist,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.em,
    fontFeatureSettings = if (tabular) TABULAR else null
)

/** The app's type scale, sized like iOS text styles. Every changing number is tabular. */
@Immutable
data class StromXType(
    val largeTitle: TextStyle = style(34, 41, FontWeight.Bold, 0.01),
    val title: TextStyle = style(22, 28, FontWeight.Bold),
    val headline: TextStyle = style(17, 22, FontWeight.SemiBold),
    val body: TextStyle = style(17, 22, FontWeight.Normal),
    val subhead: TextStyle = style(15, 20, FontWeight.Normal),
    val footnote: TextStyle = style(13, 18, FontWeight.Normal),
    val caption: TextStyle = style(12, 16, FontWeight.Normal),
    /** Upper-case card and section labels. */
    val label: TextStyle = style(13, 16, FontWeight.SemiBold, 0.03),
    val tab: TextStyle = style(10, 12, FontWeight.Medium),
    /** Live speed on the Ride screen. */
    val speed: TextStyle = style(104, 108, FontWeight.SemiBold, -0.04, tabular = true),
    /** Tile readings: fuel, engine, top speed, total. */
    val reading: TextStyle = style(28, 32, FontWeight.SemiBold, tabular = true),
    val readingLarge: TextStyle = style(40, 46, FontWeight.SemiBold, tabular = true),
    val number: TextStyle = style(17, 22, FontWeight.SemiBold, tabular = true),
    /** Always-on display: thin so few pixels are lit. */
    val aodClock: TextStyle = style(76, 80, FontWeight.ExtraLight, -0.01, tabular = true),
    val aodSpeed: TextStyle = style(150, 150, FontWeight.ExtraLight, -0.04, tabular = true)
)

/** Material components (text fields, dialogs, snackbars) pick these up. */
val MaterialTypography = Typography(
    displayLarge = style(57, 64, FontWeight.Normal),
    headlineSmall = style(24, 32, FontWeight.Bold),
    titleLarge = style(22, 28, FontWeight.Bold),
    titleMedium = style(17, 22, FontWeight.SemiBold),
    titleSmall = style(15, 20, FontWeight.SemiBold),
    bodyLarge = style(17, 22, FontWeight.Normal),
    bodyMedium = style(15, 20, FontWeight.Normal),
    bodySmall = style(13, 18, FontWeight.Normal),
    labelLarge = style(17, 22, FontWeight.SemiBold),
    labelMedium = style(13, 16, FontWeight.Medium),
    labelSmall = style(11, 14, FontWeight.Medium)
)
