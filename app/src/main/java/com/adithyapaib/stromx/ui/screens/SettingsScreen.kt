package com.adithyapaib.stromx.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adithyapaib.stromx.garage.FuelEstimator
import com.adithyapaib.stromx.nav.NavigationController
import com.adithyapaib.stromx.settings.Appearance
import com.adithyapaib.stromx.settings.DistanceUnit
import com.adithyapaib.stromx.settings.PairedBike
import com.adithyapaib.stromx.settings.SettingsViewModel
import com.adithyapaib.stromx.ui.components.GroupedSection
import com.adithyapaib.stromx.ui.components.Gutter
import com.adithyapaib.stromx.ui.components.IconTile
import com.adithyapaib.stromx.ui.components.ListRow
import com.adithyapaib.stromx.ui.components.MinTouch
import com.adithyapaib.stromx.ui.components.RowDivider
import com.adithyapaib.stromx.ui.components.ScreenTitle
import com.adithyapaib.stromx.ui.components.Segmented
import com.adithyapaib.stromx.ui.components.StromXIcons
import com.adithyapaib.stromx.ui.components.SwitchRow
import com.adithyapaib.stromx.ui.theme.StromX
import java.util.Locale

/** Preferences the Settings tab edits, with their setters. */
data class SettingsState(
    val pairedBike: PairedBike?,
    val connected: Boolean,
    val riderName: String,
    val tankLitres: Double?,
    val appearance: Appearance,
    val unit: DistanceUnit,
    val keepScreenOn: Boolean,
    val alwaysOnDisplay: Boolean
)

@Composable
fun SettingsScreen(
    state: SettingsState,
    navigation: NavigationController,
    onForgetBike: () -> Unit,
    onRiderNameChange: (String) -> Unit,
    onTankChange: (Double) -> Unit,
    onAppearanceChange: (Appearance) -> Unit,
    onUnitChange: (DistanceUnit) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onAlwaysOnChange: (Boolean) -> Unit,
    onPreviewAlwaysOn: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = StromX.colors
    val context = LocalContext.current
    var confirmForget by remember { mutableStateOf(false) }
    var editTank by remember { mutableStateOf(false) }

    val navEnabled by navigation.enabled.collectAsStateWithLifecycle()
    val listenerConnected by navigation.listenerConnected.collectAsStateWithLifecycle()
    // Access is granted in system settings, so re-check whenever the screen comes back.
    var accessGranted by remember { mutableStateOf(false) }
    LifecycleResumeEffect(Unit) {
        accessGranted = NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
        onPauseOrDispose { }
    }
    val hasAccess = accessGranted || listenerConnected

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Gutter)
            .padding(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ScreenTitle("Settings")

        GroupedSection {
            val bike = state.pairedBike
            ListRow(
                title = "V-Strom SX",
                subtitle = when {
                    state.connected -> "Connected" + (bike?.name?.takeIf { it.isNotEmpty() }?.let { " · ${it.take(5)}·····" } ?: "")
                    bike != null -> "Paired · not connected"
                    else -> "Not paired yet · connects to the first V-Strom it finds"
                },
                leading = { IconTile(StromXIcons.Bike, background = c.ink, tint = c.onInk, size = 40.dp) }
            )
            RowDivider()
            ListRow(
                "Fuel tank",
                value = String.format(Locale.getDefault(), "%.1f L", state.tankLitres ?: FuelEstimator.DEFAULT_TANK_LITRES),
                showChevron = true,
                onClick = { editTank = true }
            )
            if (bike != null) {
                RowDivider()
                ListRow("Forget this bike", titleColor = c.tint, onClick = { confirmForget = true })
            }
        }

        GroupedSection(footer = "Shown on the cluster as a greeting when the bike connects.") {
            Row(
                Modifier.fillMaxWidth().heightIn(min = MinTouch).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Name on cluster", color = c.label, style = StromX.type.body)
                BasicTextField(
                    value = state.riderName,
                    onValueChange = onRiderNameChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = StromX.type.body.copy(color = c.labelTertiary, textAlign = TextAlign.End),
                    cursorBrush = SolidColor(c.tintFill),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    decorationBox = { inner ->
                        androidx.compose.foundation.layout.Box(contentAlignment = Alignment.CenterEnd) {
                            if (state.riderName.isEmpty()) {
                                Text(SettingsViewModel.DEFAULT_RIDER_NAME, Modifier.fillMaxWidth(), color = c.labelTertiary, style = StromX.type.body, textAlign = TextAlign.End)
                            }
                            inner()
                        }
                    }
                )
            }
        }

        GroupedSection(header = "Display") {
            ChoiceRow("Appearance") {
                Segmented(
                    options = listOf("Auto", "Light", "Black"),
                    selected = state.appearance.ordinal,
                    onSelect = { onAppearanceChange(Appearance.entries[it]) },
                    modifier = Modifier.width(200.dp),
                    track = c.fill.takeIf { c.isDark } ?: c.segment
                )
            }
            RowDivider()
            ChoiceRow("Units") {
                Segmented(
                    options = listOf("km · °C", "mi · °F"),
                    selected = state.unit.ordinal,
                    onSelect = { onUnitChange(DistanceUnit.entries[it]) },
                    modifier = Modifier.width(150.dp),
                    track = c.fill.takeIf { c.isDark } ?: c.segment
                )
            }
            RowDivider()
            SwitchRow("Keep screen on while riding", state.keepScreenOn, onKeepScreenOnChange)
        }

        GroupedSection(
            header = "Always-on ride display",
            footer = "While connected, StromX dims to a black screen with speed, the next turn and fuel instead of letting the phone sleep. Double-tap to wake. Best on AMOLED screens; it uses some battery."
        ) {
            SwitchRow("Always-on ride display", state.alwaysOnDisplay, onAlwaysOnChange)
            RowDivider()
            ListRow("Preview", titleColor = c.tint, onClick = onPreviewAlwaysOn)
        }

        GroupedSection(
            header = "Navigation",
            footer = "Start directions in Google Maps and StromX shows the next turn on the bike's display."
        ) {
            SwitchRow("Send Maps turns to cluster", navEnabled, navigation::setEnabled)
            if (navEnabled) {
                RowDivider()
                ListRow(
                    "Notification access",
                    value = if (hasAccess) "Allowed" else "Needed",
                    showChevron = !hasAccess,
                    onClick = if (hasAccess) null else {
                        {
                            try {
                                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            } catch (_: ActivityNotFoundException) {
                                // No settings screen for this on the device; nothing else to offer.
                            }
                        }
                    }
                )
            }
        }

        GroupedSection {
            ListRow("Diagnostics", subtitle = "Bluetooth link, frames and logs", showChevron = true, onClick = onOpenDiagnostics)
            RowDivider()
            ListRow("Version", value = appVersion(context))
        }
    }

    if (confirmForget) {
        ConfirmDialog(
            title = "Forget this bike?",
            text = "StromX disconnects and pairs with the next V-Strom SX it finds.",
            confirm = "Forget",
            onConfirm = { onForgetBike(); confirmForget = false },
            onDismiss = { confirmForget = false }
        )
    }

    if (editTank) {
        var text by remember { mutableStateOf(String.format(Locale.US, "%.1f", state.tankLitres ?: FuelEstimator.DEFAULT_TANK_LITRES)) }
        val parsed = text.replace(',', '.').toDoubleOrNull()?.takeIf { it in 5.0..40.0 }
        AlertDialog(
            onDismissRequest = { editTank = false },
            title = { Text("Fuel tank") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Used for fuel left and range. The V-Strom SX tank holds about 12 L.", style = StromX.type.subhead)
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        singleLine = true,
                        suffix = { Text("L") },
                        isError = parsed == null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            },
            confirmButton = {
                TextButton(enabled = parsed != null, onClick = { parsed?.let(onTankChange); editTank = false }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editTank = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ChoiceRow(title: String, control: @Composable () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = MinTouch).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, Modifier.weight(1f), color = StromX.colors.label, style = StromX.type.body)
        control()
    }
}

private fun appVersion(context: android.content.Context): String = try {
    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "—"
} catch (_: Exception) {
    "—"
}
