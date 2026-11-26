package com.adithyapaib.stromx.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adithyapaib.stromx.garage.DueState
import com.adithyapaib.stromx.garage.FuelMath
import com.adithyapaib.stromx.garage.FuelRecord
import com.adithyapaib.stromx.garage.MaintenanceStatus
import com.adithyapaib.stromx.settings.DistanceUnit
import com.adithyapaib.stromx.ui.components.CardLabel
import com.adithyapaib.stromx.ui.components.Chip
import com.adithyapaib.stromx.ui.components.GroupedSection
import com.adithyapaib.stromx.ui.components.Gutter
import com.adithyapaib.stromx.ui.components.ListRow
import com.adithyapaib.stromx.ui.components.MinTouch
import com.adithyapaib.stromx.ui.components.PrimaryButton
import com.adithyapaib.stromx.ui.components.ProgressBar
import com.adithyapaib.stromx.ui.components.RowDivider
import com.adithyapaib.stromx.ui.components.ScreenTitle
import com.adithyapaib.stromx.ui.components.SecondaryButton
import com.adithyapaib.stromx.ui.components.Segmented
import com.adithyapaib.stromx.ui.components.StromCard
import com.adithyapaib.stromx.ui.components.StromXIcons
import com.adithyapaib.stromx.ui.formatDay
import com.adithyapaib.stromx.ui.formatOdometer
import com.adithyapaib.stromx.ui.formatRupees
import com.adithyapaib.stromx.ui.formatWhole
import com.adithyapaib.stromx.ui.rememberNowMs
import com.adithyapaib.stromx.ui.theme.Palette
import com.adithyapaib.stromx.ui.theme.StromX
import java.util.Locale

enum class GarageSection { Fuel, Service }

@Composable
fun GarageScreen(
    section: GarageSection,
    onSectionChange: (GarageSection) -> Unit,
    fuel: List<FuelRecord>,
    statuses: List<MaintenanceStatus>,
    unit: DistanceUnit,
    onLogFillUp: () -> Unit,
    onDeleteFuel: (Long) -> Unit,
    onMarkDone: (Long) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onAddReminder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val due = statuses.count { it.state == DueState.Due || it.state == DueState.Soon }
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Gutter)
            .padding(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ScreenTitle("Garage")
        Segmented(
            options = listOf("Fuel", "Service"),
            selected = section.ordinal,
            onSelect = { onSectionChange(GarageSection.entries[it]) },
            modifier = Modifier.fillMaxWidth(),
            badges = if (due > 0) mapOf(GarageSection.Service.ordinal to due) else emptyMap()
        )
        when (section) {
            GarageSection.Fuel -> FuelPane(fuel, onLogFillUp, onDeleteFuel)
            GarageSection.Service -> ServicePane(statuses, unit, onMarkDone, onDeleteItem, onAddReminder)
        }
    }
}

// ---------------------------------------------------------------- Fuel

@Composable
private fun FuelPane(records: List<FuelRecord>, onLogFillUp: () -> Unit, onDelete: (Long) -> Unit) {
    val c = StromX.colors
    val now = rememberNowMs(60_000L)
    val summary = remember(records, now) { FuelMath.summary(records, now) }
    val segments = remember(records) { FuelMath.segments(records).sortedBy { it.endAtMs } }
    val economyById = remember(segments) { segments.associate { it.endRecordId to it.kmPerLitre } }
    var pendingDelete by remember { mutableStateOf<FuelRecord?>(null) }

    StromCard(Modifier.fillMaxWidth(), padding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 16.dp), verticalSpacing = 14.dp) {
        Row(verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                CardLabel("Economy")
                val avg = summary.avgKmPerLitre
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(avg?.let { String.format(Locale.getDefault(), "%.1f", it) } ?: "—", color = c.label, style = StromX.type.readingLarge)
                    if (avg != null) Text("km/L", Modifier.padding(bottom = 6.dp), color = c.labelTertiary, style = StromX.type.headline.copy(fontWeight = FontWeight.Medium))
                }
                if (avg == null) {
                    Text("Needs two full-tank fill-ups", color = c.labelTertiary, style = StromX.type.footnote)
                }
            }
            EconomyBars(segments.takeLast(6).map { it.kmPerLitre })
        }
        Box(Modifier.fillMaxWidth().height(0.5.dp).background(c.separator))
        Row(Modifier.fillMaxWidth()) {
            Stat(summary.avgCostPerKm?.let { formatRupees(it, decimals = 2) } ?: "—", "per km", Modifier.weight(1f))
            Stat(formatRupees(summary.thisMonthCost), "this month", Modifier.weight(1f))
        }
    }

    PrimaryButton("Log fill-up", onLogFillUp, icon = StromXIcons.Plus)

    if (records.isEmpty()) {
        GroupedSection(header = "Fill-ups") {
            Text(
                "Nothing logged yet. At the pump, log litres, cost and whether you filled to the brim.",
                Modifier.padding(16.dp),
                color = c.labelTertiary,
                style = StromX.type.subhead
            )
        }
    } else {
        GroupedSection(header = "Fill-ups", footer = "Tap a fill-up to delete it.") {
            records.forEachIndexed { index, r ->
                if (index > 0) RowDivider()
                val economy = economyById[r.id]
                ListRow(
                    title = formatDay(r.atMs),
                    subtitle = buildString {
                        append(String.format(Locale.getDefault(), "%.1f L", r.litres))
                        append(if (r.fullTank) " · full tank" else " · top-up")
                        if (economy != null) append(String.format(Locale.getDefault(), " · %.1f km/L", economy))
                    },
                    trailing = { Text(formatRupees(r.cost), color = c.label, style = StromX.type.number) },
                    onClick = { pendingDelete = r }
                )
            }
        }
    }

    pendingDelete?.let { record ->
        ConfirmDialog(
            title = "Delete this fill-up?",
            text = "${formatDay(record.atMs)} · ${String.format(Locale.getDefault(), "%.1f L", record.litres)} · ${formatRupees(record.cost)}. Economy is recalculated without it.",
            confirm = "Delete",
            onConfirm = { onDelete(record.id); pendingDelete = null },
            onDismiss = { pendingDelete = null }
        )
    }
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(value, color = StromX.colors.label, style = StromX.type.title.copy(fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum"))
        Text(label, color = StromX.colors.labelTertiary, style = StromX.type.footnote)
    }
}

/** The last few full-to-full economies as small bars; the newest is filled. */
@Composable
private fun EconomyBars(values: List<Double>) {
    if (values.isEmpty()) return
    val c = StromX.colors
    val max = values.max()
    val min = values.min()
    Row(
        Modifier
            .height(44.dp)
            .semantics { contentDescription = "Last ${values.size} full tanks, ${String.format(Locale.getDefault(), "%.1f to %.1f", min, max)} km per litre" },
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        values.forEachIndexed { index, v ->
            val fraction = if (max > min) (0.45 + 0.55 * (v - min) / (max - min)).toFloat() else 0.8f
            Box(
                Modifier
                    .width(10.dp)
                    .height(44.dp * fraction)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (index == values.lastIndex) c.ink else c.fill)
            )
        }
    }
}

// ---------------------------------------------------------------- Service

/** "420 km left · or 12 days", "Overdue by 120 km", "Not started". */
fun serviceRemaining(status: MaintenanceStatus, unit: DistanceUnit): String {
    if (status.state == DueState.NotStarted) return "Not started · tap to mark done"
    val parts = mutableListOf<String>()
    status.remainingKm?.let { km ->
        parts += if (km >= 0) "${formatOdometer(km, unit)} ${unit.distance} left" else "Overdue by ${formatOdometer(-km, unit)} ${unit.distance}"
    }
    status.remainingDays?.let { days ->
        parts += when {
            days < 0 -> "overdue by ${-days} ${if (days == -1) "day" else "days"}"
            parts.isEmpty() -> "$days ${if (days == 1) "day" else "days"} left"
            else -> "or $days ${if (days == 1) "day" else "days"}"
        }
    }
    return parts.joinToString(" · ").replaceFirstChar { it.uppercase() }
}

private fun interval(status: MaintenanceStatus): String {
    val item = status.item
    val km = item.intervalKm?.let { "${formatWhole(it)} km" }
    val days = item.intervalDays?.let { "$it days" }
    return "every " + listOfNotNull(km, days).joinToString(" or ")
}

@Composable
private fun ServicePane(
    statuses: List<MaintenanceStatus>,
    unit: DistanceUnit,
    onMarkDone: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onAdd: () -> Unit
) {
    val c = StromX.colors
    var selected by remember { mutableStateOf<MaintenanceStatus?>(null) }
    val focus = statuses.firstOrNull { it.state == DueState.Due || it.state == DueState.Soon }

    if (focus != null) {
        StromCard(Modifier.fillMaxWidth(), padding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 16.dp), verticalSpacing = 12.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (focus.state == DueState.Due) Chip("Overdue", background = Palette.Blood, color = Palette.White)
                else Chip("Due soon")
                Text(interval(focus), Modifier.weight(1f), color = c.labelTertiary, style = StromX.type.footnote, textAlign = androidx.compose.ui.text.style.TextAlign.End)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(focus.item.name, color = c.label, style = StromX.type.title)
                Text(serviceRemaining(focus, unit), color = c.labelSecondary, style = StromX.type.subhead)
            }
            ProgressBar(focus.used, color = c.tintFill, height = 8.dp)
            SecondaryButton("Mark as done today", onClick = { onMarkDone(focus.item.id) })
        }
    }

    val rest = statuses.filter { it !== focus }
    GroupedSection(
        header = if (focus != null) "Upcoming" else "Reminders",
        footer = if (statuses.isEmpty()) "Reminders count down from the bike's odometer and the calendar." else null
    ) {
        rest.forEachIndexed { index, s ->
            if (index > 0) RowDivider()
            Column(
                Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button) { selected = s }
                    .heightIn(min = MinTouch)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(s.item.name, Modifier.weight(1f), color = c.label, style = StromX.type.body)
                    Text(serviceRemaining(s, unit), color = c.labelTertiary, style = StromX.type.subhead)
                }
                if (s.state != DueState.NotStarted) {
                    ProgressBar(s.used, color = if (s.state == DueState.Ok) c.ink else c.tintFill)
                }
            }
        }
        if (rest.isNotEmpty()) RowDivider()
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onAdd)
                .heightIn(min = MinTouch)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            androidx.compose.material3.Icon(StromXIcons.Plus, contentDescription = null, Modifier.width(18.dp), tint = c.tint)
            Text("Add reminder", color = c.tint, style = StromX.type.body)
        }
    }

    selected?.let { s ->
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(s.item.name) },
            text = { Text(serviceRemaining(s, unit) + "\n" + interval(s).replaceFirstChar { it.uppercase() }) },
            confirmButton = {
                TextButton(onClick = { onMarkDone(s.item.id); selected = null }) { Text("Mark done today") }
            },
            dismissButton = {
                TextButton(onClick = { onDelete(s.item.id); selected = null }) { Text("Delete", color = if (c.isDark) Palette.BloodText else Palette.Blood) }
            }
        )
    }
}

@Composable
fun ConfirmDialog(title: String, text: String, confirm: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirm, color = if (StromX.colors.isDark) Palette.BloodText else Palette.Blood)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
