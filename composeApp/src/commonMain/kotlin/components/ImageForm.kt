@file:OptIn(ExperimentalEncodingApi::class)

package components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import compose.icons.TablerIcons
import compose.icons.tablericons.CameraPlus
import imageService.processImage
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import getFileLoader
import printService.ImageElement
import printService.PrintElement
import kotlin.io.encoding.ExperimentalEncodingApi
import getContext
import imageService.ImageOptions
import imageService.ProcessResponse
import imageService.uploadImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import printService.PrintRequest
import showToast
import ui.SwitchGroup
import kotlin.math.roundToInt

val HEIGHT_RANGE = 50f..2000f
const val MAX_SIZE = 384

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun ImageForm(onValueChange: (el: PrintRequest) -> Unit) {
    var showFilePicker by remember { mutableStateOf(false) }
    var keepAspectRatio by remember { mutableStateOf(true) }
    var customHeight by remember { mutableStateOf(MAX_SIZE.toFloat()) }
    var rotateRight by remember { mutableStateOf(false) }

    var uploadedUuid by remember { mutableStateOf<String?>(null) }
    var imagePreview by remember { mutableStateOf<ImageBitmap?>(null) }
    var isUploadLoading by remember { mutableStateOf(false) }
    var isPreviewLoading by remember { mutableStateOf(false) }

    val fileType = listOf("jpg", "png")
    val context = getContext()
    val coroutineScope = rememberCoroutineScope()
    val loadFile = getFileLoader()

    val painter = BitmapPainter(imagePreview ?: ImageBitmap(1, 1))

    fun onFileChanged(path: String) {
        coroutineScope.launch {
            isUploadLoading = true
            try {
                val originalImage = loadFile(path)
                uploadedUuid = uploadImage(originalImage)
            } catch (e: Exception) {
                println(e)
                showToast(context, "Error while loading image")
            }
            isUploadLoading = false
        }
    }

    suspend fun updateImage() {
        if (isUploadLoading || uploadedUuid == null) return

        isPreviewLoading = true

        try {
            val response = processImage(uploadedUuid!!, ImageOptions(if (rotateRight) 90 else 0, keepAspectRatio, customHeight))
            imagePreview = constructProcessedImage(response)

            val elements = response.chunksBase64.map { ImageElement(it) }.reversed()
            onValueChange(PrintRequest(elements, true))
        } catch(e: Exception) {
            println(e)
            showToast(context, "Error while processing image")
            uploadedUuid = null // reset state
        }

        isPreviewLoading = false
    }

    LaunchedEffect(keepAspectRatio, uploadedUuid, customHeight, rotateRight) {
        coroutineScope.launch {
            updateImage()
        }
    }

    FilePicker(show = showFilePicker, fileExtensions = fileType) { platformFile ->
        showFilePicker = false
        if (platformFile != null) {
            onFileChanged(platformFile.path)
        }
    }


    Column(
        Modifier.fillMaxHeight().padding(top = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isUploadLoading) {
            CircularProgressIndicator()
            return
        }

        if (uploadedUuid == null) {
            Box(Modifier.fillMaxHeight().padding(bottom = 150.dp), Alignment.Center) {
                FilledTonalButton(onClick = { showFilePicker = true }) {
                    Icon(TablerIcons.CameraPlus, "choose image")
                    Spacer(Modifier.width(10.dp))
                    Text("Choose image")
                }
            }
            return
        }

        Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            if (isPreviewLoading) {
                CircularProgressIndicator()
            } else if (imagePreview != null) {
                Image(
                    painter,
                    contentDescription = "image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        FilledTonalButton(onClick = { showFilePicker = true }) {
            Text("Change image")
        }
        Spacer(Modifier.height(20.dp))
        SwitchGroup(rotateRight, { v -> rotateRight = v }, { Text("Rotate 90deg") })
        SwitchGroup(keepAspectRatio, { v -> keepAspectRatio = v }, { Text("Keep aspect ratio") })
        if (!keepAspectRatio) {
            Slider(customHeight, { v -> customHeight = v }, valueRange = HEIGHT_RANGE, steps = 100)
            Text("Height: ${customHeight.roundToInt()}", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
        }
    }
}

suspend fun constructProcessedImage(data: ProcessResponse): ImageBitmap = withContext(Dispatchers.Default) {
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