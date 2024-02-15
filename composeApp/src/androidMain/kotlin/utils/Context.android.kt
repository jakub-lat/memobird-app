package utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

actual typealias MyContext = android.content.Context

@Composable
actual fun getContext(): MyContext = LocalContext.current
