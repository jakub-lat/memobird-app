import android.net.Uri
import androidx.compose.runtime.Composable
import java.io.File

@Composable
actual fun getFileLoader(): (path: String) -> ByteArray {
    val context = getContext()
    return { path ->
        val stream = context.contentResolver.openInputStream(Uri.parse(path))!!
        val bytes = stream.readBytes()
        stream.close()
        bytes
    }
}