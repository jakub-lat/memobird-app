package ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SwitchGroup(checked: Boolean, onCheckedChange: (v: Boolean) -> Unit, label: @Composable() (() -> Unit)) {
    Surface(onClick = { onCheckedChange(!checked) }, color = Color.Transparent, modifier = Modifier.clip(RoundedCornerShape(5.dp))) {
        Row(Modifier.fillMaxWidth().padding(10.dp, 3.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            label()
            Switch(checked, onCheckedChange)
        }
    }
}