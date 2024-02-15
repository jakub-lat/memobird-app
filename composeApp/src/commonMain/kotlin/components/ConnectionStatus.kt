package components

import WIFI_PASSWORD
import WIFI_SSID_PREFIX
import printService.WifiManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import utils.getContext
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionStatus(wifi: WifiManager) {
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = getContext()
    val wifiState by wifi.state.collectAsState()

    val connectedColors = AssistChipDefaults.assistChipColors(Color(0xFFd3f2d2), leadingIconContentColor = Color(0xFF45d843))
    val disconnectedColors = AssistChipDefaults.assistChipColors(MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp))

    fun hideSheet() {
        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                showBottomSheet = false
            }
        }
    }

    fun connect() = coroutineScope.launch {
        try {
            wifi.connect(WIFI_SSID_PREFIX, WIFI_PASSWORD)
        } catch (_: Exception) {

        }
    }

    fun disconnect() = coroutineScope.launch {
        wifi.unbind()
        wifi.disconnect()
        hideSheet()
    }

    ElevatedAssistChip(
        onClick = {
            showBottomSheet = true
        },
        label = { Text(if (wifiState.isConnected) "Connected" else "No device") },
        leadingIcon = {
            Icon(Icons.Filled.Circle, "connection status", Modifier.size(12.dp))
        },
        shape = RoundedCornerShape(20.dp),
        colors = if (wifiState.isConnected) connectedColors else disconnectedColors,
        elevation = AssistChipDefaults.assistChipElevation()
    )

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(
                Modifier.fillMaxWidth().padding(20.dp, 10.dp, 20.dp, 50.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (wifiState.isConnected) {
                    Text("Connected to Memobird", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(5.dp))
                    Text("SSID: ${wifiState.ssid}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(30.dp))
                    FilledTonalButton(onClick = { disconnect() }) {
                        Text("Disconnect")
                    }
                } else {
                    Text("No device connected", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(30.dp))
                    Button(onClick = { connect() }) {
                        Text("Connect to Memobird")
                    }
                }
            }
        }
    }
}