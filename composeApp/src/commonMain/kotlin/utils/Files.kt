package utils

import androidx.compose.runtime.Composable

@Composable
expect fun getFileLoader(): (path: String) -> ByteArray