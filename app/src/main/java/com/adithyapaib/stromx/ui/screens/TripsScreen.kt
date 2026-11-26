package com.adithyapaib.stromx.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adithyapaib.stromx.ride.RideMath
import com.adithyapaib.stromx.ride.RideRecord
import com.adithyapaib.stromx.settings.DistanceUnit
import com.adithyapaib.stromx.ui.components.CardLabel
import com.adithyapaib.stromx.ui.components.GroupedSection
import com.adithyapaib.stromx.ui.components.Gutter
import com.adithyapaib.stromx.ui.components.ListRow
import com.adithyapaib.stromx.ui.components.MinTouch
import com.adithyapaib.stromx.ui.components.PrimaryButton
import com.adithyapaib.stromx.ui.components.RowDivider
import com.adithyapaib.stromx.ui.components.ScreenTitle
import com.adithyapaib.stromx.ui.components.StromCard
import com.adithyapaib.stromx.ui.formatRupees
import com.adithyapaib.stromx.ui.formatTrip
import com.adithyapaib.stromx.ui.formatWhen
import com.adithyapaib.stromx.ui.isSameMonth
import com.adithyapaib.stromx.ui.isThisWeek
import com.adithyapaib.stromx.ui.monthLabel
import com.adithyapaib.stromx.ui.rememberNowMs
import com.adithyapaib.stromx.ui.speedIn
import com.adithyapaib.stromx.ui.theme.Palette
import com.adithyapaib.stromx.ui.theme.StromX
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun TripsScreen(
    rides: List<RideRecord>,
    odometerKm: Int?,
    unit: DistanceUnit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onDiscard: () -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val c = StromX.colors
    val now = rememberNowMs()
    val active = rides.firstOrNull { it.isActive }
    val finished = rides.filter { !it.isActive }
    var selected by remember { mutableStateOf<RideRecord?>(null) }
    var confirmDiscard by remember { mutableStateOf(false) }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Gutter)
            .padding(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ScreenTitle("Trips")

        if (active != null) {
            ActiveRide(active, odometerKm, unit, now, onStop, onDiscard = { confirmDiscard = true })
        } else {
            PrimaryButton("Start ride", onStart)
        }

        val month = finished.filter { isSameMonth(it.startedAtMs, now) }
        StromCard(Modifier.fillMaxWidth(), padding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 16.dp), verticalSpacing = 12.dp) {
            CardLabel(monthLabel(now))
            Row(Modifier.fillMaxWidth()) {
                MonthStat(com.adithyapaib.stromx.ui.formatWhole((month.sumOf { it.distanceKm ?: 0.0 } * unit.perKm).roundToInt()),"${unit.distance} ridden", Modifier.weight(1f))
                MonthStat("${month.size}", if (month.size == 1) "ride" else "rides", Modifier.weight(1f))
                val hours = month.sumOf { it.durationMs ?: 0L } / 3_600_000.0
                MonthStat(if (hours >= 10) "${hours.roundToInt()} h" else String.format(Locale.getDefault(), "%.1f h", hours), "in the saddle", Modifier.weight(1f))
            }
        }

        if (finished.isEmpty()) {
            GroupedSection(header = "Rides") {
                Text(
                    "No rides yet. Tap Start ride before you set off; StromX records distance and top speed from the bike.",
                    Modifier.padding(16.dp),
                    color = c.labelTertiary,
                    style = StromX.type.subhead
                )
            }
        } else {
            val (week, earlier) = finished.partition { isThisWeek(it.startedAtMs, now) }
            if (week.isNotEmpty()) RideGroup("This week", week, unit, now) { selected = it }
            if (earlier.isNotEmpty()) RideGroup("Earlier", earlier, unit, now) { selected = it }
        }
    }

    selected?.let { ride ->
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(formatWhen(ride.startedAtMs, now)) },
            text = { Text(rideDetail(ride, unit)) },
            confirmButton = { TextButton(onClick = { selected = null }) { Text("Done") } },
            dismissButton = {
                TextButton(onClick = { onDelete(ride.id); selected = null }) {
                    Text("Delete", color = if (c.isDark) Palette.BloodText else Palette.Blood)
                }
            }
        )
    }

    if (confirmDiscard) {
        ConfirmDialog(
            title = "Discard this ride?",
            text = "It stops recording and isn't saved.",
            confirm = "Discard",
            onConfirm = { onDiscard(); confirmDiscard = false },
            onDismiss = { confirmDiscard = false }
        )
    }
}

@Composable
private fun ActiveRide(
    ride: RideRecord,
    odometerKm: Int?,
    unit: DistanceUnit,
    now: Long,
    onStop: () -> Unit,
    onDiscard: () -> Unit
) {
    val c = StromX.colors
    val distance = ride.startOdometerKm?.let { start -> odometerKm?.let { (it - start).coerceAtLeast(0).toDouble() } }
    val meta = listOfNotNull(
        RideMath.formatDuration(now - ride.startedAtMs),
        distance?.let { "${formatTrip(it, unit)} ${unit.distance}" },
        ride.maxSpeedKph.takeIf { it > 0 }?.let { "top ${speedIn(unit, it)} ${unit.speed}" }
    ).joinToString(" · ")

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(c.hero)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(Palette.Blue))
            Column(Modifier.weight(1f)) {
                Text("Riding now", color = Palette.White, style = StromX.type.headline)
                Text(meta, color = Palette.Silver, style = StromX.type.footnote.copy(fontFeatureSettings = "tnum"))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onStop,
                modifier = Modifier.weight(1f).heightIn(min = MinTouch),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Palette.White, contentColor = Palette.Night)
            ) { Text("Stop and save", style = StromX.type.headline) }
            TextButton(onClick = onDiscard, modifier = Modifier.heightIn(min = MinTouch)) {
                Text("Discard", color = Palette.Silver, style = StromX.type.body)
            }
        }
    }
}

@Composable
private fun MonthStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(value, color = StromX.colors.label, style = StromX.type.reading.copy(fontSize = 26.sp))
        Text(label, color = StromX.colors.labelTertiary, style = StromX.type.footnote)
    }
}

@Composable
private fun RideGroup(title: String, rides: List<RideRecord>, unit: DistanceUnit, now: Long, onSelect: (RideRecord) -> Unit) {
    GroupedSection(header = title) {
        rides.forEachIndexed { index, ride ->
            if (index > 0) RowDivider()
            ListRow(
                title = formatWhen(ride.startedAtMs, now),
                subtitle = listOfNotNull(
                    ride.durationMs?.let(RideMath::formatDuration),
                    ride.avgSpeedKph?.let { "avg ${(it * unit.perKm).roundToInt()} ${unit.speed}" },
                    ride.maxSpeedKph.takeIf { it > 0 }?.let { "top ${speedIn(unit, it)}" }
                ).joinToString(" · "),
                trailing = {
                    Text(
                        ride.distanceKm?.let { "${formatTrip(it, unit)} ${unit.distance}" } ?: "—",
                        color = StromX.colors.label,
                        style = StromX.type.number
                    )
                },
                showChevron = true,
                onClick = { onSelect(ride) }
            )
        }
    }
}

private fun rideDetail(ride: RideRecord, unit: DistanceUnit): String = listOfNotNull(
    ride.distanceKm?.let { "Distance: ${formatTrip(it, unit)} ${unit.distance}" } ?: "Distance: not reported by the bike",
    ride.durationMs?.let { "Time: ${RideMath.formatDuration(it)}" },
    ride.avgSpeedKph?.let { "Average: ${(it * unit.perKm).roundToInt()} ${unit.speed}" },
    ride.maxSpeedKph.takeIf { it > 0 }?.let { "Top speed: ${speedIn(unit, it)} ${unit.speed}" },
    ride.fuelUsedLitres?.let { String.format(Locale.getDefault(), "Fuel: about %.1f L", it) + (ride.fuelCost?.let { cost -> " (${formatRupees(cost)})" } ?: "") }
).joinToString("\n")
