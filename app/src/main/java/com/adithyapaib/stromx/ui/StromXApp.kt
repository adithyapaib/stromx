package com.adithyapaib.stromx.ui

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adithyapaib.stromx.ble.BleConnectionState
import com.adithyapaib.stromx.ble.BleViewModel
import com.adithyapaib.stromx.garage.DueState
import com.adithyapaib.stromx.garage.GarageViewModel
import com.adithyapaib.stromx.settings.SettingsViewModel
import com.adithyapaib.stromx.ui.components.CountBadge
import com.adithyapaib.stromx.ui.components.StromXIcons
import com.adithyapaib.stromx.ui.screens.AddReminderSheet
import com.adithyapaib.stromx.ui.screens.AodScreen
import com.adithyapaib.stromx.ui.screens.DiagnosticsScreen
import com.adithyapaib.stromx.ui.screens.FillUpSheet
import com.adithyapaib.stromx.ui.screens.GarageScreen
import com.adithyapaib.stromx.ui.screens.GarageSection
import com.adithyapaib.stromx.ui.screens.RideScreen
import com.adithyapaib.stromx.ui.screens.RideState
import com.adithyapaib.stromx.ui.screens.SettingsScreen
import com.adithyapaib.stromx.ui.screens.SettingsState
import com.adithyapaib.stromx.ui.screens.TripsScreen
import com.adithyapaib.stromx.ui.theme.StromX
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AppTab(val label: String, val icon: ImageVector) {
    Ride("Ride", StromXIcons.Gauge),
    Trips("Trips", StromXIcons.Route),
    Garage("Garage", StromXIcons.Wrench),
    Settings("Settings", StromXIcons.Gear)
}

/** With the always-on display enabled, this long without a touch switches to it. */
private const val ALWAYS_ON_AFTER_MS = 30_000L

@Composable
fun StromXApp(viewModel: BleViewModel, settings: SettingsViewModel, garage: GarageViewModel) {
    val c = StromX.colors
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
    val lastUpdateMs by viewModel.lastTelemetryAtMs.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val verbose by viewModel.verbose.collectAsStateWithLifecycle()
    val pairedBike by viewModel.pairedBike.collectAsStateWithLifecycle()
    val instruction by viewModel.navigation.instruction.collectAsStateWithLifecycle()
    val unit by settings.unit.collectAsStateWithLifecycle()
    val keepScreenOn by settings.keepScreenOn.collectAsStateWithLifecycle()
    val riderName by settings.riderName.collectAsStateWithLifecycle()
    val appearance by settings.appearance.collectAsStateWithLifecycle()
    val alwaysOn by settings.alwaysOnDisplay.collectAsStateWithLifecycle()
    val fuelRecords by garage.fuel.collectAsStateWithLifecycle()
    val rides by garage.rides.collectAsStateWithLifecycle()
    val serviceStatuses by garage.service.collectAsStateWithLifecycle()
    val lastOdometerKm by garage.odometerKm.collectAsStateWithLifecycle()
    val fillUpPrompt by garage.fillUpPrompt.collectAsStateWithLifecycle()
    val fuelEstimate by garage.estimate.collectAsStateWithLifecycle()
    val tankLitres by garage.tankLitres.collectAsStateWithLifecycle()
    val topSpeed by garage.topSpeed.collectAsStateWithLifecycle()

    // The garage counts down from the bike's own odometer and watches the fuel bars for fill-ups.
    LaunchedEffect(telemetry) { telemetry?.let(garage::onTelemetry) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { garage.refresh() }

    var tab by rememberSaveable { mutableStateOf(AppTab.Ride) }
    var garageSection by rememberSaveable { mutableStateOf(GarageSection.Fuel) }
    var showDiagnostics by rememberSaveable { mutableStateOf(false) }
    var fillUpOpen by rememberSaveable { mutableStateOf(false) }
    var addReminderOpen by rememberSaveable { mutableStateOf(false) }
    var alwaysOnPreview by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val haptics = LocalHapticFeedback.current
    val view = LocalView.current
    val connected = connectionState == BleConnectionState.CONNECTED
    val dueCount = serviceStatuses.count { it.state == DueState.Due || it.state == DueState.Soon }

    // Errors from the Bluetooth layer.
    LaunchedEffect(viewModel) {
        viewModel.errors.collect { message ->
            snackbar.currentSnackbarData?.dismiss()
            launch { snackbar.showSnackbar(message) }
        }
    }

    // The gauge jumped by two or more bars: offer to log the fill-up.
    LaunchedEffect(fillUpPrompt) {
        val prompt = fillUpPrompt ?: return@LaunchedEffect
        if (fillUpOpen) return@LaunchedEffect
        val result = snackbar.showSnackbar(
            message = "Fuel gauge rose to ${prompt.fuelBars} bars",
            actionLabel = "Log fill-up",
            duration = SnackbarDuration.Long
        )
        if (result == SnackbarResult.ActionPerformed) fillUpOpen = true
    }

    // A buzz when the link drops, since the rider may not be looking at the screen.
    var previousState by remember { mutableStateOf(connectionState) }
    LaunchedEffect(connectionState) {
        if (previousState == BleConnectionState.CONNECTED && connectionState != BleConnectionState.CONNECTED) {
            haptics.performHapticFeedback(HapticFeedbackType.Reject)
        }
        previousState = connectionState
    }

    // Always-on display: after a while untouched while connected, or when previewed from Settings.
    // The last touch lives outside Compose state so touches don't recompose the app; only the
    // idle flag does, and only when it flips.
    val lastTouch = remember { LongArray(1) { System.currentTimeMillis() } }
    var idle by remember { mutableStateOf(false) }
    LaunchedEffect(alwaysOn, connected) {
        // Count from now, so connecting never drops straight into the always-on display.
        lastTouch[0] = System.currentTimeMillis()
        idle = false
        if (!alwaysOn || !connected) return@LaunchedEffect
        while (true) {
            delay(1_000L)
            val untouched = System.currentTimeMillis() - lastTouch[0] > ALWAYS_ON_AFTER_MS
            if (untouched != idle) idle = untouched
        }
    }
    val showAlwaysOn = alwaysOnPreview || (alwaysOn && connected && idle && !fillUpOpen && !addReminderOpen)
    val wake = {
        alwaysOnPreview = false
        lastTouch[0] = System.currentTimeMillis()
        idle = false
    }
    val monthKm = remember(rides) {
        val now = System.currentTimeMillis()
        rides.filter { !it.isActive && isSameMonth(it.startedAtMs, now) }.sumOf { it.distanceKm ?: 0.0 }
    }

    val holdScreen = connected && (keepScreenOn || alwaysOn)
    DisposableEffect(holdScreen, alwaysOnPreview) {
        view.keepScreenOn = holdScreen || alwaysOnPreview
        onDispose { view.keepScreenOn = false }
    }
    DimWindowWhile(showAlwaysOn)

    BackHandler(enabled = showAlwaysOn) { wake() }
    BackHandler(enabled = !showAlwaysOn && showDiagnostics) { showDiagnostics = false }
    // Material back behaviour: another tab returns to Ride before leaving the app.
    BackHandler(enabled = !showAlwaysOn && !showDiagnostics && tab != AppTab.Ride) { tab = AppTab.Ride }

    val askNotifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    Box(Modifier.fillMaxSize().background(c.background)) {
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    // Watches every touch without consuming it, to know when the rider is idle.
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent(PointerEventPass.Initial)
                            lastTouch[0] = System.currentTimeMillis()
                            if (idle) idle = false
                        }
                    }
                }
        ) {
            Scaffold(
                containerColor = c.background,
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbar) },
                bottomBar = {
                    if (!showDiagnostics) TabBar(tab, onSelect = { tab = it }, garageBadge = dueCount)
                }
            ) { inner ->
                Box(Modifier.padding(inner).consumeWindowInsets(inner).fillMaxSize()) {
                    if (showDiagnostics) {
                        DiagnosticsScreen(
                            connectionState = connectionState,
                            lastUpdateMs = lastUpdateMs,
                            logs = logs,
                            verbose = verbose,
                            onVerboseChange = viewModel::setVerbose,
                            onClearLogs = viewModel::clearLogs,
                            onSendArrowTest = viewModel.navigation::sendTest,
                            onBack = { showDiagnostics = false }
                        )
                    } else when (tab) {
                        AppTab.Ride -> RideScreen(
                            state = RideState(
                                connectionState = connectionState,
                                telemetry = telemetry,
                                lastUpdateMs = lastUpdateMs,
                                unit = unit,
                                fuelEstimate = fuelEstimate,
                                topSpeed = topSpeed,
                                odometerKm = lastOdometerKm,
                                monthKm = monthKm,
                                lastRide = rides.firstOrNull { !it.isActive },
                                nextService = serviceStatuses.firstOrNull { it.state == DueState.Due || it.state == DueState.Soon }
                            ),
                            onConnect = viewModel::connect,
                            onFindFuel = { openFuelSearch(context) },
                            onLogFillUp = { fillUpOpen = true },
                            onOpenService = {
                                garageSection = GarageSection.Service
                                tab = AppTab.Garage
                            }
                        )
                        AppTab.Trips -> TripsScreen(
                            rides = rides,
                            odometerKm = lastOdometerKm,
                            unit = unit,
                            onStart = garage::startRide,
                            onStop = garage::stopRide,
                            onDiscard = garage::discardRide,
                            onDelete = garage::deleteRide
                        )
                        AppTab.Garage -> GarageScreen(
                            section = garageSection,
                            onSectionChange = { garageSection = it },
                            fuel = fuelRecords,
                            statuses = serviceStatuses,
                            unit = unit,
                            onLogFillUp = { fillUpOpen = true },
                            onDeleteFuel = garage::deleteFuel,
                            onMarkDone = garage::markDone,
                            onDeleteItem = garage::deleteItem,
                            onAddReminder = { addReminderOpen = true }
                        )
                        AppTab.Settings -> SettingsScreen(
                            state = SettingsState(
                                pairedBike = pairedBike,
                                connected = connected,
                                riderName = riderName,
                                tankLitres = tankLitres,
                                appearance = appearance,
                                unit = unit,
                                keepScreenOn = keepScreenOn,
                                alwaysOnDisplay = alwaysOn
                            ),
                            navigation = viewModel.navigation,
                            onForgetBike = viewModel::forgetBike,
                            onRiderNameChange = settings::setRiderName,
                            onTankChange = garage::setTankLitres,
                            onAppearanceChange = settings::setAppearance,
                            onUnitChange = settings::setUnit,
                            onKeepScreenOnChange = settings::setKeepScreenOn,
                            onAlwaysOnChange = settings::setAlwaysOnDisplay,
                            onPreviewAlwaysOn = { alwaysOnPreview = true },
                            onOpenDiagnostics = { showDiagnostics = true }
                        )
                    }
                }
            }
        }

        if (showAlwaysOn) {
            val active = rides.firstOrNull { it.isActive }
            AodScreen(
                telemetry = telemetry,
                unit = unit,
                fuelEstimate = fuelEstimate,
                instruction = instruction,
                rideKm = active?.startOdometerKm?.let { start -> lastOdometerKm?.let { (it - start).coerceAtLeast(0).toDouble() } },
                connected = connected,
                onWake = wake
            )
        }
    }

    if (fillUpOpen) {
        FillUpSheet(
            prompt = fillUpPrompt,
            odometerKm = lastOdometerKm,
            onSave = { litres, cost, odometer, full ->
                garage.addFuel(litres, cost, odometer, full)
                fillUpOpen = false
            },
            onDismiss = {
                garage.dismissFillUp()
                fillUpOpen = false
            }
        )
    }

    if (addReminderOpen) {
        AddReminderSheet(
            odometerKm = lastOdometerKm,
            onSave = { name, km, days, lastKm, lastAt ->
                garage.addItem(name, km, days, lastKm, lastAt)
                addReminderOpen = false
                // Reminders arrive as notifications; ask the first time one is saved.
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            onDismiss = { addReminderOpen = false }
        )
    }
}

@Composable
private fun TabBar(selected: AppTab, onSelect: (AppTab) -> Unit, garageBadge: Int) {
    val c = StromX.colors
    Column(Modifier.fillMaxWidth().background(c.tabBar)) {
        Box(Modifier.fillMaxWidth().height(0.5.dp).background(c.separator))
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(58.dp)
                .selectableGroup()
        ) {
            AppTab.entries.forEach { tab ->
                val isSelected = tab == selected
                val tint = if (isSelected) c.tint else c.labelTertiary
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(tab) }),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box {
                        Icon(tab.icon, contentDescription = null, Modifier.size(26.dp), tint = tint)
                        if (tab == AppTab.Garage && garageBadge > 0) {
                            CountBadge(garageBadge, Modifier.align(Alignment.TopEnd).offset(x = 10.dp, y = (-4).dp))
                        }
                    }
                    Text(
                        tab.label,
                        Modifier.padding(top = 3.dp),
                        color = tint,
                        style = StromX.type.tab.copy(fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.SemiBold else androidx.compose.ui.text.font.FontWeight.Medium)
                    )
                }
            }
        }
    }
}

/** Drops the screen to minimum brightness while [active], then hands brightness back to the system. */
@Composable
private fun DimWindowWhile(active: Boolean) {
    val view = LocalView.current
    DisposableEffect(active) {
        val window = (view.context as? Activity)?.window
        if (active && window != null) {
            window.attributes = window.attributes.apply { screenBrightness = 0.02f }
        }
        onDispose {
            window?.let { it.attributes = it.attributes.apply { screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE } }
        }
    }
}

/** Petrol stations near the rider, in whatever maps app handles geo: links. */
private fun openFuelSearch(context: android.content.Context) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=petrol+pump")))
    } catch (_: ActivityNotFoundException) {
        // No maps app installed.
    }
}
