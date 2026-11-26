package com.adithyapaib.stromx.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adithyapaib.stromx.ble.BleConnectionState
import com.adithyapaib.stromx.ui.components.GroupedSection
import com.adithyapaib.stromx.ui.components.Gutter
import com.adithyapaib.stromx.ui.components.ListRow
import com.adithyapaib.stromx.ui.components.MinTouch
import com.adithyapaib.stromx.ui.components.RowDivider
import com.adithyapaib.stromx.ui.components.StromXIcons
import com.adithyapaib.stromx.ui.components.SwitchRow
import com.adithyapaib.stromx.ui.formatAge
import com.adithyapaib.stromx.ui.rememberNowMs
import com.adithyapaib.stromx.ui.theme.StromX

/** The Bluetooth link for debugging: state, frames, verbose logging and the raw log. */
@Composable
fun DiagnosticsScreen(
    connectionState: BleConnectionState,
    lastUpdateMs: Long,
    logs: List<String>,
    verbose: Boolean,
    onVerboseChange: (Boolean) -> Unit,
    onClearLogs: () -> Unit,
    onSendArrowTest: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = StromX.colors
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val now = rememberNowMs()
    val newestFirst = remember(logs) { logs.asReversed() }
    var arrowByte by remember { mutableIntStateOf(com.adithyapaib.stromx.nav.Maneuver.STRAIGHT.clusterByte) }
    val connected = connectionState == BleConnectionState.CONNECTED

    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = Gutter, end = Gutter, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(MinTouch)) {
                    Icon(StromXIcons.Back, contentDescription = "Back to Settings", tint = c.tint)
                }
                Text("Diagnostics", color = c.label, style = StromX.type.title)
            }
        }
        item {
            GroupedSection {
                ListRow("State", value = connectionState.name.lowercase().replaceFirstChar { it.uppercase() })
                RowDivider()
                ListRow("Last frame", value = if (lastUpdateMs == 0L) "None yet" else formatAge((now - lastUpdateMs).coerceAtLeast(0L)))
                RowDivider()
                ListRow("Service · write · notify", value = "FFF0 · FFF1 · FFF2")
                RowDivider()
                SwitchRow(
                    "Verbose BLE logging",
                    verbose,
                    onVerboseChange,
                    subtitle = "GATT table, reads, RSSI, link parameters and a per-byte breakdown of each frame."
                )
            }
        }
        item {
            GroupedSection(
                header = "Cluster arrow test",
                footer = "Bike parked. Sends one turn arrow byte to the cluster so each glyph can be checked."
            ) {
                Row(
                    Modifier.fillMaxWidth().heightIn(min = MinTouch).padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = { arrowByte = (arrowByte - 1).coerceAtLeast(0) }, modifier = Modifier.heightIn(min = MinTouch)) { Text("−") }
                    Text("$arrowByte", Modifier.width(48.dp), color = c.label, style = StromX.type.number, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    OutlinedButton(onClick = { arrowByte = (arrowByte + 1).coerceAtMost(MAX_TEST_BYTE) }, modifier = Modifier.heightIn(min = MinTouch)) { Text("+") }
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { onSendArrowTest(arrowByte) }, enabled = connected) {
                            Text(if (connected) "Send" else "Not connected", style = StromX.type.headline)
                        }
                    }
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("ACTIVITY", Modifier.weight(1f).padding(start = 4.dp), color = c.labelTertiary, style = StromX.type.footnote)
                TextButton(onClick = { clipboard.setText(AnnotatedString(logs.joinToString("\n"))) }, enabled = logs.isNotEmpty()) { Text("Copy") }
                TextButton(
                    onClick = {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "StromX BLE log")
                            putExtra(Intent.EXTRA_TEXT, logs.joinToString("\n"))
                        }
                        context.startActivity(Intent.createChooser(send, "Share BLE log"))
                    },
                    enabled = logs.isNotEmpty()
                ) { Text("Share") }
                TextButton(onClick = onClearLogs, enabled = logs.isNotEmpty()) { Text("Clear") }
            }
        }
        if (newestFirst.isEmpty()) {
            item {
                Text(
                    "Nothing yet. Bluetooth events will appear here.",
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(c.card).padding(16.dp),
                    color = c.labelTertiary,
                    style = StromX.type.subhead
                )
            }
        } else {
            items(newestFirst) { line ->
                // Log lines are raw data, so they stay monospaced.
                Text(
                    line,
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(c.card).padding(horizontal = 12.dp, vertical = 8.dp),
                    color = c.labelSecondary,
                    style = StromX.type.caption.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp, lineHeight = 15.sp)
                )
            }
        }
    }
}

private const val MAX_TEST_BYTE = 75
