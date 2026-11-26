package com.adithyapaib.stromx.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.adithyapaib.stromx.ble.VStromTelemetry
import com.adithyapaib.stromx.garage.FuelEstimate
import com.adithyapaib.stromx.nav.NavInstruction
import com.adithyapaib.stromx.settings.DistanceUnit
import com.adithyapaib.stromx.ui.components.StromXIcons
import com.adithyapaib.stromx.ui.formatClock
import com.adithyapaib.stromx.ui.formatLongDay
import com.adithyapaib.stromx.ui.formatTrip
import com.adithyapaib.stromx.ui.rememberNowMs
import com.adithyapaib.stromx.ui.speedIn
import com.adithyapaib.stromx.ui.theme.Palette
import com.adithyapaib.stromx.ui.theme.StromX
import java.util.Locale
import kotlin.math.roundToInt

/** Dim text on the black ground; still 6:1 on #000. */
private val Dim = Color(0xFF8A8F91)

/**
 * The always-on ride display: pure black so an AMOLED panel lights only the digits, thin
 * weights so few pixels are on, and the whole layout drifts a few dp each minute against
 * burn-in. Double-tap wakes the app.
 */
@Composable
fun AodScreen(
    telemetry: VStromTelemetry?,
    unit: DistanceUnit,
    fuelEstimate: FuelEstimate?,
    instruction: NavInstruction?,
    rideKm: Double?,
    connected: Boolean,
    onWake: () -> Unit
) {
    val now = rememberNowMs()
    val minute = (now / 60_000L).toInt()
    val shiftX = ((minute % 5) - 2) * 4
    val shiftY = (((minute / 5) % 5) - 2) * 4

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) { detectTapGestures(onDoubleTap = { onWake() }) }
            .semantics { contentDescription = "Always-on ride display. Double-tap to open StromX." }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .offset(shiftX.dp, shiftY.dp)
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(formatClock(now), color = Palette.Silver, style = StromX.type.aodClock)
            Text(formatLongDay(now), color = Palette.Heather, style = StromX.type.subhead)

            Spacer(Modifier.weight(1f))

            Text(telemetry?.let { "${speedIn(unit, it.speed)}" } ?: "—", color = Palette.Silver, style = StromX.type.aodSpeed)
            Text(unit.speed, color = Palette.Heather, style = StromX.type.headline.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Normal))

            if (instruction != null) {
                Spacer(Modifier.height(32.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(StromXIcons.TurnRight, contentDescription = null, Modifier.size(22.dp), tint = Palette.Blue)
                    Text(turnText(instruction, unit), color = Palette.Blue, style = StromX.type.title.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Normal))
                }
            }

            Spacer(Modifier.height(28.dp))
            Row(Modifier.fillMaxWidth()) {
                AodStat(fuelEstimate?.rangeKm?.let { "${(it * unit.perKm).roundToInt()}" } ?: telemetry?.takeIf { it.isFuelValid }?.let { "${it.fuelBars}/6" } ?: "—",
                    if (fuelEstimate?.rangeKm != null) "${unit.distance} of fuel" else "fuel bars", Modifier.weight(1f))
                AodStat(telemetry?.engineTempC?.let { "${unit.temperatureFromCelsius(it)}°" } ?: "—", "engine", Modifier.weight(1f))
                AodStat(rideKm?.let { formatTrip(it, unit) } ?: telemetry?.let { formatTrip(it.tripA, unit) } ?: "—",
                    if (rideKm != null) "${unit.distance} ride" else "${unit.distance} trip A", Modifier.weight(1f))
            }

            Spacer(Modifier.weight(1f))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(if (connected) Palette.Blue else Dim))
                Text(if (connected) "V-Strom SX connected" else "Bike disconnected", color = Palette.Heather, style = StromX.type.footnote)
            }
            Spacer(Modifier.height(8.dp))
            Text("Double-tap to wake", color = Dim, style = StromX.type.caption, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun AodStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, color = Palette.Silver, style = StromX.type.title.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Light, fontFeatureSettings = "tnum"))
        Text(label, color = Palette.Heather, style = StromX.type.caption)
    }
}

/** "Turn right in 400 m", "Keep left in 1.2 km". */
private fun turnText(step: NavInstruction, unit: DistanceUnit): String {
    val meters = step.distanceMeters ?: return step.maneuver.label
    val distance = if (unit == DistanceUnit.MILES) {
        val miles = meters / 1609.34
        if (miles < 0.1) "${(meters * 3.28084).roundToInt()} ft" else String.format(Locale.getDefault(), "%.1f mi", miles)
    } else {
        if (meters < 1000) "$meters m" else String.format(Locale.getDefault(), "%.1f km", meters / 1000.0)
    }
    return "${step.maneuver.label} in $distance"
}
