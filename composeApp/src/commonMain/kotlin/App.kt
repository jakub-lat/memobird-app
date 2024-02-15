import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import components.ConnectionStatus
import components.ImageForm
import components.TextForm
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import printService.*
import ui.SegmentedButtonItem
import ui.SegmentedButtons

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
@Composable
fun App() {
    val printer = remember { HttpPrinter(MEMOBIRD_URL) }
    var selectedIndex by remember { mutableStateOf(0) }
    var request by remember { mutableStateOf(PrintRequest(emptyList(), false)) }
    val context = getContext()
    val wifi = remember { getWifiManager(context) }
    val coroutineScope = rememberCoroutineScope()

    fun print() {
        if (request.elements.isEmpty()) {
            return
        }

        coroutineScope.launch {
            try {
                wifi.connect(WIFI_SSID_PREFIX, WIFI_PASSWORD)
                wifi.bind()

                request.print(printer)

                showToast(context, "Printing...")
            } catch (e: Exception) {
                println(e)
                showToast(context, "Error: ${e.message}")
            }
            wifi.unbind()
        }
    }

    MaterialTheme {
        RequirePermissions(grantButton = { onClick ->
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Button(onClick) {
                    Text("Grant permissions")
                }
            }
        }) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Memobird") },
                        actions = {
                            ConnectionStatus(wifi)
                            Spacer(Modifier.width(10.dp))
                        }
                    )
                },
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        modifier = Modifier
                            .padding(16.dp)
                            .navigationBarsPadding()
                            .imePadding(),
                        onClick = { print() },
                        containerColor = MaterialTheme.colorScheme.primary,
                        icon = { Icon(TablerIcons.Printer, "Print") },
                        text = { Text(text = "Print") },
                    )
                }
            ) { innerPadding ->
                Box(Modifier.padding(innerPadding)) {
                    Column(
                        Modifier.fillMaxWidth().padding(20.dp, 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SegmentedButtons {
                            SegmentedButtonItem(
                                selected = selectedIndex == 0,
                                onClick = { selectedIndex = 0 },
                                label = { Text("Text") },
                                icon = { Icon(TablerIcons.Typography, "Image") },
                            )
                            SegmentedButtonItem(
                                selected = selectedIndex == 1,
                                onClick = { selectedIndex = 1 },
                                label = { Text("Image") },
                                icon = { Icon(TablerIcons.Photo, "Image") },
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                        when (selectedIndex) {
                            0 -> TextForm(onValueChange = { v -> request = v })
                            1 -> ImageForm(onValueChange = { v -> request = v })
                        }
                        Spacer(Modifier.height(50.dp))
                    }
                }
            }
        }
    }
}