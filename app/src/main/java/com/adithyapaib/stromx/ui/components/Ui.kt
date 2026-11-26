package com.adithyapaib.stromx.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adithyapaib.stromx.ui.theme.Palette
import com.adithyapaib.stromx.ui.theme.StromX

/** Side gutter for every screen. */
val Gutter = 16.dp

/** Glove-sized minimum for anything tappable. */
val MinTouch = 56.dp

/** The large iOS-style title at the top of a tab, with an optional trailing element. */
@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier, trailing: @Composable RowScope.() -> Unit = {}) {
    Row(
        modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, Modifier.weight(1f), color = StromX.colors.label, style = StromX.type.largeTitle)
        trailing()
    }
}

/** A rounded card on the grouped background. */
@Composable
fun StromCard(
    modifier: Modifier = Modifier,
    radius: Dp = 22.dp,
    color: Color = StromX.colors.card,
    padding: PaddingValues = PaddingValues(16.dp),
    verticalSpacing: Dp = 10.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier
            .clip(RoundedCornerShape(radius))
            .background(color)
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        content = content
    )
}

/** Small upper-case label at the top of a card, with an optional leading icon. */
@Composable
fun CardLabel(text: String, icon: ImageVector? = null, color: Color = StromX.colors.labelTertiary, trailing: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (icon != null) Icon(icon, contentDescription = null, Modifier.size(16.dp), tint = color)
        Text(text.uppercase(), Modifier.weight(1f), color = color, style = StromX.type.label)
        if (trailing != null) Text(trailing, color = color, style = StromX.type.footnote)
    }
}

/** An inset grouped list: optional header above, optional footer below. */
@Composable
fun GroupedSection(
    modifier: Modifier = Modifier,
    header: String? = null,
    footer: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (header != null) {
            Text(
                header.uppercase(),
                Modifier.padding(horizontal = 20.dp),
                color = StromX.colors.labelTertiary,
                style = StromX.type.footnote
            )
        }
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(StromX.colors.card),
            content = content
        )
        if (footer != null) {
            Text(
                footer,
                Modifier.padding(horizontal = 16.dp),
                color = StromX.colors.labelTertiary,
                style = StromX.type.footnote
            )
        }
    }
}

/** Hairline between rows of a grouped list. */
@Composable
fun RowDivider(indent: Dp = 16.dp) {
    Box(
        Modifier
            .padding(start = indent)
            .fillMaxWidth()
            .height(0.5.dp)
            .background(StromX.colors.separator)
    )
}

@Composable
fun Chevron() {
    Icon(StromXIcons.Chevron, contentDescription = null, Modifier.size(14.dp), tint = StromX.colors.chevron)
}

/** A square icon tile, as in iOS Settings rows. */
@Composable
fun IconTile(icon: ImageVector, background: Color, tint: Color = Palette.White, size: Dp = 30.dp) {
    Box(
        Modifier.size(size).clip(RoundedCornerShape(8.dp)).background(background),
        contentAlignment = Alignment.Center
    ) { Icon(icon, contentDescription = null, Modifier.size(size * 0.55f), tint = tint) }
}

/** One row of a grouped list. Clickable when [onClick] is set. */
@Composable
fun ListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: String? = null,
    titleColor: Color = StromX.colors.label,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    showChevron: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = MinTouch)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick, role = Role.Button) else Modifier)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        leading?.invoke()
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = titleColor, style = StromX.type.body)
            if (subtitle != null) Text(subtitle, color = StromX.colors.labelTertiary, style = StromX.type.footnote)
        }
        if (value != null) {
            Text(
                value,
                Modifier.widthIn(max = 180.dp),
                color = StromX.colors.labelTertiary,
                style = StromX.type.body,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        trailing?.invoke()
        if (showChevron) Chevron()
    }
}

/** A row with a switch; the whole row toggles. */
@Composable
fun SwitchRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, subtitle: String? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = MinTouch)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = StromX.colors.label, style = StromX.type.body)
            if (subtitle != null) Text(subtitle, color = StromX.colors.labelTertiary, style = StromX.type.footnote)
        }
        StromSwitch(checked = checked, onCheckedChange = null)
    }
}

@Composable
fun StromSwitch(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?) {
    val c = StromX.colors
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Palette.White,
            checkedTrackColor = c.tintFill,
            checkedBorderColor = c.tintFill,
            uncheckedThumbColor = Palette.White,
            uncheckedTrackColor = c.fill,
            uncheckedBorderColor = c.fill
        )
    )
}

/** iOS-style segmented control. [badges] puts a small count next to an option. */
@Composable
fun Segmented(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    badges: Map<Int, Int> = emptyMap(),
    track: Color = StromX.colors.segment
) {
    val c = StromX.colors
    Row(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(track)
            .padding(2.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selected
            Row(
                Modifier
                    .weight(1f)
                    .heightIn(min = 36.dp)
                    .then(
                        if (isSelected) Modifier.shadow(1.dp, RoundedCornerShape(8.dp)).background(c.segmentSelected, RoundedCornerShape(8.dp))
                        else Modifier
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(index) }),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    label,
                    color = c.label,
                    style = if (isSelected) StromX.type.footnote.copy(fontWeight = FontWeight.SemiBold)
                    else StromX.type.footnote
                )
                val count = badges[index] ?: 0
                if (count > 0) {
                    Spacer(Modifier.width(6.dp))
                    CountBadge(count)
                }
            }
        }
    }
}

/** Small count: inverse of the text colour, so it reads in both schemes. */
@Composable
fun CountBadge(count: Int, modifier: Modifier = Modifier) {
    Box(
        modifier
            .heightIn(min = 18.dp)
            .widthIn(min = 18.dp)
            .clip(CircleShape)
            .background(StromX.colors.label)
            .padding(horizontal = 5.dp)
            .semantics { contentDescription = "$count due" },
        contentAlignment = Alignment.Center
    ) {
        Text("$count", color = StromX.colors.card, style = StromX.type.caption.copy(fontWeight = FontWeight.Bold))
    }
}

/** The one filled action on a screen. Bold 19 sp so white on electric blue stays readable. */
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, enabled: Boolean = true) {
    val c = StromX.colors
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().heightIn(min = MinTouch),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = c.tintFill,
            contentColor = c.onTint,
            disabledContainerColor = c.fill,
            disabledContentColor = c.labelTertiary
        )
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = StromX.type.headline.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold))
    }
}

/** A quieter action: tinted text on a soft tint. */
@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, neutral: Boolean = false) {
    val c = StromX.colors
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = MinTouch),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (neutral) c.fill else c.tintSoft,
            contentColor = if (neutral) c.label else c.tint
        )
    ) { Text(text, style = StromX.type.headline) }
}

/** The bike's 6-bar fuel gauge, drawn as the cluster does: filled bars from the left. */
@Composable
fun FuelBars(bars: Int, modifier: Modifier = Modifier, height: Dp = 22.dp, filled: Color = StromX.colors.ink) {
    Row(
        modifier.fillMaxWidth().semantics { contentDescription = "Fuel gauge $bars of 6 bars" },
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(6) { index ->
            Box(
                Modifier
                    .weight(1f)
                    .height(height)
                    .clip(RoundedCornerShape(5.dp))
                    .background(if (index < bars) filled else StromX.colors.fill)
            )
        }
    }
}

/** A horizontal bar filled to [fraction] (0..1). */
@Composable
fun ProgressBar(fraction: Float, modifier: Modifier = Modifier, color: Color = StromX.colors.ink, track: Color = StromX.colors.fill, height: Dp = 6.dp) {
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(track)
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(height / 2))
                .background(color)
        )
    }
}

/** A capsule with a status dot, e.g. the connection pill on Ride. */
@Composable
fun StatusPill(text: String, dot: Color, textColor: Color = StromX.colors.label, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(CircleShape)
            .background(StromX.colors.card)
            .padding(start = 10.dp, end = 12.dp, top = 7.dp, bottom = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(dot))
        Text(text, color = textColor, style = StromX.type.footnote.copy(fontWeight = FontWeight.SemiBold))
    }
}

/** A small upper-case chip, e.g. "DUE SOON". */
@Composable
fun Chip(text: String, background: Color = StromX.colors.tintSoft, color: Color = StromX.colors.tint) {
    Text(
        text.uppercase(),
        Modifier.clip(RoundedCornerShape(6.dp)).background(background).padding(horizontal = 8.dp, vertical = 3.dp),
        color = color,
        style = StromX.type.caption.copy(fontWeight = FontWeight.Bold)
    )
}
