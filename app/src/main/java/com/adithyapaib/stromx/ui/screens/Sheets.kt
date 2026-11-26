package com.adithyapaib.stromx.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import com.adithyapaib.stromx.garage.FillUpPrompt
import com.adithyapaib.stromx.garage.MaintenancePresets
import com.adithyapaib.stromx.ui.components.GroupedSection
import com.adithyapaib.stromx.ui.components.Gutter
import com.adithyapaib.stromx.ui.components.ListRow
import com.adithyapaib.stromx.ui.components.MinTouch
import com.adithyapaib.stromx.ui.components.PrimaryButton
import com.adithyapaib.stromx.ui.components.RowDivider
import com.adithyapaib.stromx.ui.components.StromXIcons
import com.adithyapaib.stromx.ui.components.SwitchRow
import com.adithyapaib.stromx.ui.formatWhole
import com.adithyapaib.stromx.ui.theme.StromX

/** Parses "9.4" or "9,4"; null when empty or not a positive number. */
private fun parsePositive(text: String): Double? =
    text.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 }

/** A label on the left, an editable value on the right, as in iOS forms. */
@Composable
private fun FieldRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Decimal,
    suffix: String? = null
) {
    val c = StromX.colors
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = MinTouch)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(label, color = c.label, style = StromX.type.body)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = StromX.type.body.copy(color = c.label, textAlign = TextAlign.End, fontFeatureSettings = "tnum"),
            cursorBrush = SolidColor(c.tintFill),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterEnd) {
                    if (value.isEmpty()) {
                        Text(placeholder, Modifier.fillMaxWidth(), color = c.labelTertiary, style = StromX.type.body, textAlign = TextAlign.End)
                    }
                    inner()
                }
            }
        )
        if (suffix != null) Text(suffix, color = c.labelTertiary, style = StromX.type.body)
    }
}

@Composable
private fun SheetHeader(title: String, onCancel: () -> Unit, onSave: () -> Unit, canSave: Boolean) {
    val c = StromX.colors
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onCancel) { Text("Cancel", color = c.tint, style = StromX.type.body) }
        Text(title, Modifier.weight(1f), color = c.label, style = StromX.type.headline, textAlign = TextAlign.Center)
        TextButton(onClick = onSave, enabled = canSave) {
            Text("Save", color = if (canSave) c.tint else c.labelTertiary, style = StromX.type.headline)
        }
    }
}

/** Log a fill-up. Opens with the odometer from the bike, and with a note when the gauge jumped. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillUpSheet(
    prompt: FillUpPrompt?,
    odometerKm: Int?,
    onSave: (litres: Double, cost: Double, odometerKm: Int, fullTank: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val c = StromX.colors
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var litres by rememberSaveable { mutableStateOf("") }
    var cost by rememberSaveable { mutableStateOf("") }
    var odometer by rememberSaveable { mutableStateOf((prompt?.odometerKm ?: odometerKm)?.toString().orEmpty()) }
    var fullTank by rememberSaveable { mutableStateOf(true) }

    val parsedLitres = parsePositive(litres)
    val parsedCost = cost.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0 }
    val parsedOdometer = odometer.filter(Char::isDigit).toIntOrNull()?.takeIf { it > 0 }
    val canSave = parsedLitres != null && parsedCost != null && parsedOdometer != null
    val save = {
        if (parsedLitres != null && parsedCost != null && parsedOdometer != null) {
            onSave(parsedLitres, parsedCost, parsedOdometer, fullTank)
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet, containerColor = c.background) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = Gutter)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            SheetHeader("Log fill-up", onDismiss, save, canSave)

            if (prompt != null) {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(c.tintSoft).padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(StromXIcons.Fuel, contentDescription = null, Modifier.size(20.dp), tint = c.tint)
                    Text(
                        "The gauge just rose to ${prompt.fuelBars} bars. Did you fill up?",
                        color = c.labelSecondary,
                        style = StromX.type.subhead
                    )
                }
            }

            GroupedSection(footer = if (odometerKm != null || prompt != null) "Odometer is filled in from the bike." else null) {
                FieldRow("Litres", litres, { litres = it }, "0.0", suffix = "L")
                RowDivider()
                FieldRow("Cost", cost, { cost = it }, "0", suffix = "₹")
                RowDivider()
                FieldRow("Odometer", odometer, { odometer = it.filter(Char::isDigit) }, "km", KeyboardType.Number, suffix = "km")
            }

            GroupedSection(footer = "Economy is measured from one full tank to the next.") {
                SwitchRow("Full tank", fullTank, { fullTank = it })
            }

            PrimaryButton("Save fill-up", save, enabled = canSave)
        }
    }
}

/** Add a service reminder, from a preset or from scratch. Counting starts today. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderSheet(
    odometerKm: Int?,
    onSave: (name: String, intervalKm: Int?, intervalDays: Int?, lastDoneKm: Int?, lastDoneAtMs: Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val c = StromX.colors
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by rememberSaveable { mutableStateOf("") }
    var km by rememberSaveable { mutableStateOf("") }
    var days by rememberSaveable { mutableStateOf("") }
    var doneToday by rememberSaveable { mutableStateOf(true) }

    val intervalKm = km.filter(Char::isDigit).toIntOrNull()?.takeIf { it > 0 }
    val intervalDays = days.filter(Char::isDigit).toIntOrNull()?.takeIf { it > 0 }
    val canSave = name.isNotBlank() && (intervalKm != null || intervalDays != null)
    val save = {
        if (canSave) {
            onSave(
                name.trim(),
                intervalKm,
                intervalDays,
                if (doneToday) odometerKm else null,
                if (doneToday) System.currentTimeMillis() else null
            )
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet, containerColor = c.background) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = Gutter)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            SheetHeader("New reminder", onDismiss, save, canSave)

            GroupedSection(footer = "Leave either interval empty to count by the other only.") {
                FieldRow("Name", name, { name = it }, "Engine oil", KeyboardType.Text)
                RowDivider()
                FieldRow("Every", km, { km = it.filter(Char::isDigit) }, "3000", KeyboardType.Number, suffix = "km")
                RowDivider()
                FieldRow("Or every", days, { days = it.filter(Char::isDigit) }, "180", KeyboardType.Number, suffix = "days")
            }

            GroupedSection(
                footer = if (odometerKm != null) "Counts from today at ${formatWhole(odometerKm)} km." else "Counts from today; km start once the bike reports its odometer."
            ) {
                SwitchRow("Done today", doneToday, { doneToday = it })
            }

            GroupedSection(header = "Common jobs", footer = "Starting points, not Suzuki's schedule. Check your service book.") {
                MaintenancePresets.forEachIndexed { index, preset ->
                    if (index > 0) RowDivider()
                    ListRow(
                        title = preset.name,
                        value = listOfNotNull(preset.intervalKm?.let { "${formatWhole(it)} km" }, preset.intervalDays?.let { "$it d" }).joinToString(" · "),
                        onClick = {
                            name = preset.name
                            km = preset.intervalKm?.toString().orEmpty()
                            days = preset.intervalDays?.toString().orEmpty()
                        }
                    )
                }
            }

            PrimaryButton("Save reminder", save, enabled = canSave)
            Box(Modifier.width(1.dp))
        }
    }
}
