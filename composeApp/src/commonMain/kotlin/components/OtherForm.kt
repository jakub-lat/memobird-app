package components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Qrcode
import printService.PrintRequest
import ui.SegmentedButtonItem
import ui.SegmentedButtons

@Composable
fun OtherForm(onValueChange: (request: PrintRequest) -> Unit) {
    var selectedIndex by remember { mutableStateOf(0) }

    Column(Modifier.padding(0.dp, 0.dp).fillMaxWidth()) {
        SegmentedButtons(Modifier.height(40.dp)) {
            SegmentedButtonItem(
                selected = selectedIndex == 0,
                onClick = { selectedIndex = 0 },
                label = { Text("QR Code") },
                icon = { Icon(TablerIcons.Qrcode, "QR code") }
            )
            SegmentedButtonItem(
                selected = selectedIndex == 1,
                onClick = { selectedIndex = 1 },
                label = { Text("Something else") },
            )
        }
        Spacer(Modifier.height(20.dp))
        when (selectedIndex) {
            0 -> QRCodeForm(onValueChange)
        }
    }

}