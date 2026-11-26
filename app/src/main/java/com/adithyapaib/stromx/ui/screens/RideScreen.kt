package com.adithyapaib.stromx.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adithyapaib.stromx.ble.BleConnectionState
import com.adithyapaib.stromx.ble.VStromTelemetry
import com.adithyapaib.stromx.garage.DueState
import com.adithyapaib.stromx.garage.FuelEstimate
import com.adithyapaib.stromx.garage.MaintenanceStatus
import com.adithyapaib.stromx.garage.TopSpeed
import com.adithyapaib.stromx.ride.RideMath
import com.adithyapaib.stromx.ride.RideRecord
import com.adithyapaib.stromx.settings.DistanceUnit
import com.adithyapaib.stromx.ui.STALE_AFTER_MS
import com.adithyapaib.stromx.ui.components.CardLabel
import com.adithyapaib.stromx.ui.components.Chevron
import com.adithyapaib.stromx.ui.components.FuelBars
import com.adithyapaib.stromx.ui.components.GroupedSection
import com.adithyapaib.stromx.ui.components.Gutter
import com.adithyapaib.stromx.ui.components.IconTile
import com.adithyapaib.stromx.ui.components.ListRow
import com.adithyapaib.stromx.ui.components.MinTouch
import com.adithyapaib.stromx.ui.components.PrimaryButton
import com.adithyapaib.stromx.ui.components.ProgressBar
import com.adithyapaib.stromx.ui.components.RowDivider
import com.adithyapaib.stromx.ui.components.ScreenTitle
import com.adithyapaib.stromx.ui.components.SecondaryButton
import com.adithyapaib.stromx.ui.components.StatusPill
import com.adithyapaib.stromx.ui.components.StromCard
import com.adithyapaib.stromx.ui.components.StromXIcons
import com.adithyapaib.stromx.ui.formatAge
import com.adithyapaib.stromx.ui.formatOdometer
import com.adithyapaib.stromx.ui.formatShortDate
import com.adithyapaib.stromx.ui.formatTrip
import com.adithyapaib.stromx.ui.rememberNowMs
import com.adithyapaib.stromx.ui.speedIn
import com.adithyapaib.stromx.ui.theme.ENGINE_OVERHEAT_C
import com.adithyapaib.stromx.ui.theme.Palette
import com.adithyapaib.stromx.ui.theme.StromX
import kotlin.math.roundToInt

/** Everything the Ride tab shows; the shell collects it from the view models. */
data class RideState(
    val connectionState: BleConnectionState,
    val telemetry: VStromTelemetry?,
    val lastUpdateMs: Long,
    val unit: DistanceUnit,
    val fuelEstimate: FuelEstimate?,
    val topSpeed: TopSpeed?,
    /** Last odometer the bike reported, kept while it is off. */
    val odometerKm: Int?,
    /** Distance of rides finished this calendar month, km. */
    val monthKm: Double,
    val lastRide: RideRecord?,
    /** The most urgent reminder, shown only when it is due or nearly due. */
    val nextService: MaintenanceStatus?
)

@Composable
fun RideScreen(
    state: RideState,
    onConnect: () -> Unit,
    onFindFuel: () -> Unit,
    onLogFillUp: () -> Unit,
    onOpenService: () -> Unit,
    modifier: Modifier = Modifier
) {
    val telemetry = state.telemetry
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Gutter)
            .padding(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (state.connectionState == BleConnectionState.CONNECTED && telemetry != null) {
            Connected(state, telemetry, onFindFuel, onLogFillUp, onOpenService)
        } else {
            Searching(state, onConnect)
        }
    }
}

@Composable
private fun Connected(
    state: RideState,
    t: VStromTelemetry,
    onFindFuel: () -> Unit,
    onLogFillUp: () -> Unit,
    onOpenService: () -> Unit
) {
    val c = StromX.colors
    val unit = state.unit
    val now = rememberNowMs()
    val ageMs = (now - state.lastUpdateMs).coerceAtLeast(0L)
    val stale = state.lastUpdateMs != 0L && ageMs > STALE_AFTER_MS
    val dim = if (stale) 0.45f else 1f

    ScreenTitle("Ride") {
        if (stale) StatusPill("No update · ${formatAge(ageMs)}", dot = c.off, textColor = c.labelTertiary)
        else StatusPill("V-Strom SX", dot = c.tintFill)
    }

    // Speed: the one number read at a glance.
    StromCard(Modifier.fillMaxWidth(), padding = androidx.compose.foundation.layout.PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 16.dp), verticalSpacing = 2.dp) {
        CardLabel("Speed", trailing = if (state.lastUpdateMs == 0L) null else "Updated ${formatAge(ageMs)}")
        Row(Modifier.alpha(dim), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${speedIn(unit, t.speed)}", color = c.label, style = StromX.type.speed)
            Text(unit.speed, Modifier.padding(bottom = 18.dp), color = c.labelTertiary, style = StromX.type.title.copy(fontWeight = FontWeight.Medium))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            TripText("Trip A", "${formatTrip(t.tripA, unit)} ${unit.distance}")
            TripText("Trip B", "${formatTrip(t.tripB, unit)} ${unit.distance}")
        }
    }

    TileRow {
        FuelTile(t, state.fuelEstimate, unit, dim, Modifier.weight(1f))
        EngineTile(t.engineTempC, unit, dim, Modifier.weight(1f))
    }

    TileRow {
        val top = state.topSpeed
        ReadingTile(
            label = "Top speed",
            icon = StromXIcons.Flag,
            value = top?.let { "${speedIn(unit, it.kph)}" } ?: "—",
            unitText = if (top != null) unit.speed else null,
            note = when {
                top == null -> "No reading yet"
                top.atMs != null -> "Best ever · ${formatShortDate(top.atMs)}"
                else -> "Best ever"
            },
            modifier = Modifier.weight(1f)
        )
        ReadingTile(
            label = "Total",
            icon = StromXIcons.Route,
            value = formatOdometer(t.odometer, unit),
            unitText = unit.distance,
            note = if (state.monthKm > 0) "+${formatTrip(state.monthKm, unit)} ${unit.distance} this month" else "Odometer",
            modifier = Modifier.weight(1f)
        )
    }

    TileRow {
        ActionTile("Find fuel", StromXIcons.Fuel, onFindFuel, Modifier.weight(1f))
        ActionTile("Log fill-up", StromXIcons.Plus, onLogFillUp, Modifier.weight(1f))
    }

    state.nextService?.let { ServiceNudge(it, unit, onOpenService) }
}

@Composable
private fun TripText(label: String, value: String) {
    Text(
        androidx.compose.ui.text.buildAnnotatedString {
            append("$label ")
            pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.SemiBold, color = StromX.colors.label))
            append(value)
            pop()
        },
        color = StromX.colors.labelSecondary,
        style = StromX.type.subhead.copy(fontFeatureSettings = "tnum")
    )
}

@Composable
private fun TileRow(content: @Composable RowScope.() -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), content = content)
}

@Composable
private fun ReadingTile(label: String, icon: ImageVector, value: String, unitText: String?, note: String, modifier: Modifier = Modifier) {
    StromCard(modifier, radius = 18.dp, padding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 14.dp), verticalSpacing = 6.dp) {
        CardLabel(label, icon)
        ReadingText(value, unitText)
        Text(note, color = StromX.colors.labelTertiary, style = StromX.type.footnote)
    }
}

@Composable
private fun ReadingText(value: String, unitText: String?, color: androidx.compose.ui.graphics.Color = StromX.colors.label, unitColor: androidx.compose.ui.graphics.Color = StromX.colors.labelTertiary) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(value, color = color, style = StromX.type.reading)
        if (unitText != null) {
            Text(unitText, Modifier.padding(bottom = 3.dp), color = unitColor, style = StromX.type.subhead.copy(fontWeight = FontWeight.Medium))
        }
    }
}

@Composable
private fun FuelTile(t: VStromTelemetry, estimate: FuelEstimate?, unit: DistanceUnit, dim: Float, modifier: Modifier) {
    val bars = if (t.isFuelValid) t.fuelBars else 0
    val range = estimate?.rangeKm
    StromCard(modifier, verticalSpacing = 10.dp) {
        CardLabel("Fuel", StromXIcons.Fuel)
        FuelBars(bars, Modifier.alpha(dim))
        Column {
            if (estimate != null && range != null) {
                ReadingText("${(range * unit.perKm).roundToInt()}", "${unit.distance} left")
                val margin = estimate.kmPerLitre?.let { (estimate.uncertaintyLitres * it * unit.perKm).roundToInt() }
                Text(
                    "≈ ${"%.1f".format(estimate.litres)} L" + (margin?.let { " · ±$it ${unit.distance}" } ?: ""),
                    color = StromX.colors.labelTertiary,
                    style = StromX.type.footnote
                )
            } else {
                ReadingText(if (t.isFuelValid) "$bars" else "—", if (t.isFuelValid) "of 6 bars" else null)
                Text(
                    if (estimate != null) "≈ ${"%.1f".format(estimate.litres)} L · log 2 full tanks for range" else "Log a full tank for range",
                    color = StromX.colors.labelTertiary,
                    style = StromX.type.footnote
                )
            }
        }
    }
}

/** Engine card. At [ENGINE_OVERHEAT_C] or above the whole card turns blood red and says so. */
@Composable
private fun EngineTile(tempC: Int?, unit: DistanceUnit, dim: Float, modifier: Modifier) {
    val c = StromX.colors
    val hot = tempC != null && tempC >= ENGINE_OVERHEAT_C
    val background by animateColorAsState(if (hot) Palette.Blood else c.card, tween(400), label = "engineBg")
    val foreground by animateColorAsState(if (hot) Palette.White else c.label, tween(400), label = "engineFg")
    val secondary by animateColorAsState(if (hot) Palette.BloodText else c.labelTertiary, tween(400), label = "engineSub")
    val status = when {
        tempC == null -> "No reading"
        tempC < 60 -> "Warming up"
        tempC < 110 -> "Normal · warmed up"
        tempC < ENGINE_OVERHEAT_C -> "Running hot"
        else -> "Overheating · pull over"
    }

    // A buzz once when it crosses into overheating; the rider may not be looking.
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(hot) { if (hot) haptics.performHapticFeedback(HapticFeedbackType.LongPress) }

    StromCard(
        modifier.semantics { contentDescription = "Engine ${tempC?.let { "${unit.temperatureFromCelsius(it)}${unit.temperature}" } ?: "no reading"}, $status" },
        color = background,
        verticalSpacing = 10.dp
    ) {
        CardLabel("Engine", StromXIcons.Thermometer, color = secondary)
        val fraction = tempC?.let { ((it - 40) / 110f).coerceIn(0.04f, 1f) } ?: 0f
        ProgressBar(
            fraction,
            Modifier.alpha(dim),
            color = if (hot) Palette.White else c.ink,
            track = if (hot) Palette.White.copy(alpha = 0.22f) else c.fill,
            height = 22.dp
        )
        Column {
            ReadingText(
                tempC?.let { "${unit.temperatureFromCelsius(it)}" } ?: "—",
                if (tempC != null) unit.temperature else null,
                color = foreground,
                unitColor = secondary
            )
            Text(
                status,
                color = secondary,
                style = if (hot) StromX.type.footnote.copy(fontWeight = FontWeight.Bold) else StromX.type.footnote
            )
        }
    }
}

@Composable
private fun ActionTile(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier) {
    val c = StromX.colors
    Column(
        modifier
            .heightIn(min = 84.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(c.card)
            .clickable(onClick = onClick, role = Role.Button)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        IconTile(icon, background = c.tintSoft, tint = c.tint)
        Text(label, Modifier.padding(top = 8.dp), color = c.label, style = StromX.type.headline)
    }
}

@Composable
private fun ServiceNudge(status: MaintenanceStatus, unit: DistanceUnit, onClick: () -> Unit) {
    val c = StromX.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(c.card)
            .clickable(onClick = onClick, role = Role.Button)
            .heightIn(min = MinTouch)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconTile(StromXIcons.Wrench, background = c.tintFill, tint = c.onTint)
        Column(Modifier.weight(1f)) {
            Text(
                status.item.name + if (status.state == DueState.Due) " is due" else " due soon",
                color = c.label,
                style = StromX.type.body
            )
            Text(serviceRemaining(status, unit), color = c.labelTertiary, style = StromX.type.footnote)
        }
        Chevron()
    }
}

@Composable
private fun Searching(state: RideState, onConnect: () -> Unit) {
    val c = StromX.colors
    val context = LocalContext.current
    val (title, body) = when {
        state.connectionState == BleConnectionState.ERROR ->
            "Couldn't reach the bike" to "Check that Bluetooth and the ignition are on, then try again."
        state.connectionState == BleConnectionState.CONNECTED ->
            "Waiting for the cluster" to "Connected. The first reading arrives within a few seconds."
        state.connectionState == BleConnectionState.DISCONNECTED ->
            "Bike not connected" to "Turn the ignition on and connect."
        else -> "Looking for your V-Strom" to "Turn the ignition on. StromX connects on its own when the bike is near."
    }

    ScreenTitle("Ride") {
        StatusPill(
            if (state.connectionState == BleConnectionState.CONNECTING) "Searching" else "Not connected",
            dot = c.off,
            textColor = c.labelTertiary
        )
    }

    StromCard(Modifier.fillMaxWidth(), padding = androidx.compose.foundation.layout.PaddingValues(start = 24.dp, end = 24.dp, top = 36.dp, bottom = 24.dp), verticalSpacing = 18.dp) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { BluetoothPulse(active = state.connectionState == BleConnectionState.CONNECTING) }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = c.label, style = StromX.type.title, textAlign = TextAlign.Center)
            Text(body, color = c.labelSecondary, style = StromX.type.subhead, textAlign = TextAlign.Center)
        }
        when (state.connectionState) {
            BleConnectionState.ERROR, BleConnectionState.DISCONNECTED ->
                PrimaryButton(if (state.connectionState == BleConnectionState.ERROR) "Try again" else "Connect", onConnect)
            else -> SecondaryButton(
                "Bluetooth settings",
                onClick = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                    } catch (_: ActivityNotFoundException) {
                        // No Bluetooth settings screen on this device.
                    }
                },
                neutral = true
            )
        }
    }

    val odometer = state.telemetry?.odometer?.takeIf { it > 0 } ?: state.odometerKm
    val last = state.lastRide
    if (odometer != null || last != null) {
        GroupedSection(header = "Last seen") {
            if (odometer != null) {
                ListRow("Odometer", value = "${formatOdometer(odometer, state.unit)} ${state.unit.distance}")
            }
            val bars = state.telemetry?.takeIf { it.isFuelValid }?.fuelBars
            if (bars != null) {
                RowDivider()
                ListRow("Fuel", value = "$bars of 6 bars" + (state.fuelEstimate?.rangeKm?.let { " · ${(it * state.unit.perKm).roundToInt()} ${state.unit.distance}" } ?: ""))
            }
            if (last != null && !last.isActive) {
                if (odometer != null || bars != null) RowDivider()
                ListRow(
                    "Last ride",
                    value = last.distanceKm?.let { "${formatTrip(it, state.unit)} ${state.unit.distance}" }
                        ?: last.durationMs?.let(RideMath::formatDuration)
                )
            }
        }
    }
}

/** The Bluetooth mark with two rings pulsing outwards while the phone scans. */
@Composable
private fun BluetoothPulse(active: Boolean) {
    val c = StromX.colors
    val transition = rememberInfiniteTransition(label = "pulse")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart),
        label = "phase"
    )
    Box(Modifier.size(132.dp), contentAlignment = Alignment.Center) {
        if (active) {
            Canvas(Modifier.fillMaxSize()) {
                listOf(phase, (phase + 0.5f) % 1f).forEach { p ->
                    val scale = 0.6f + p
                    drawCircle(
                        color = c.tintFill.copy(alpha = (0.55f * (1f - p)).coerceIn(0f, 1f)),
                        radius = size.minDimension / 2f * scale / 1.6f,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }
        Box(Modifier.size(72.dp).clip(CircleShape).background(c.tintSoft), contentAlignment = Alignment.Center) {
            Icon(StromXIcons.Bluetooth, contentDescription = null, Modifier.size(32.dp), tint = c.tint)
        }
    }
}
