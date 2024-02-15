package imageService

import API_URL
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import fuel.Fuel
import fuel.post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Serializable
data class ImageOptions(
    val rotate: Int = 0,
    val keepAspectRatio: Boolean = true,
    val customHeight: Float = 0f,
    val filter: String = "",
    val invert: Boolean = false
)

@Serializable
data class ProcessRequest(val uuid: String, val options: ImageOptions)

@Serializable
data class ProcessResponse(
    val chunksBase64: List<String>,
    val width: Int,
    val height: Int,
    val pixels: List<List<Boolean>>
)

@Serializable
data class UploadRequest(val imageBase64: String);

@Serializable
data class UploadResponse(val uuid: String);


val json = Json { encodeDefaults = true }
val headers = mapOf("Content-Type" to "application/json")

@OptIn(ExperimentalEncodingApi::class)
suspend fun uploadImage(file: ByteArray): String {
    val request = UploadRequest(Base64.encode(file))
    val body = json.encodeToString(request)
    val response = Fuel.post("$API_URL/upload", body = body, headers = headers).body
    val jsonResponse = Json.decodeFromString<UploadResponse>(response)
    return jsonResponse.uuid
}

@OptIn(ExperimentalEncodingApi::class)
suspend fun processImage(uuid: String, options: ImageOptions = ImageOptions()): ProcessResponse {
    val request = ProcessRequest(uuid, options)
    val body = json.encodeToString(request)
    val response = Fuel.post("$API_URL/process", body = body, headers = headers)
    if (response.statusCode == 401) {
        throw Exception("invalid session")
    }

    return Json.decodeFromString<ProcessResponse>(response.body)
}

suspend fun generateProcessedImagePreview(data: ProcessResponse): ImageBitmap =
    withContext(Dispatchers.Default) {
        val img = ImageBitmap(
            data.width,
            data.height,
            ImageBitmapConfig.Argb8888,
            false,
            ColorSpaces.Srgb
        )
        val canvas = Canvas(img)

        for (y in 0 until data.height) {
            for (x in 0 until data.width) {
                val color = if (data.pixels[y][x]) Color.Black else Color.White
                val paint = Paint().apply {
                    this.color = color
                    style = PaintingStyle.Fill
                }

                canvas.drawRect(
                    x.toFloat(),
                    data.height - y.toFloat() - 1,
                    (x + 1).toFloat(),
                    data.height - y.toFloat(),
                    paint
                )
            }
        }

        return@withContext img
    }