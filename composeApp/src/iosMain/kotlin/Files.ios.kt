import androidx.compose.runtime.Composable

@Composable
actual fun getFileLoader(): (path: String) -> ByteArray {
    return {
        byteArrayOf()
    }
}