@file:OptIn(ExperimentalEncodingApi::class)

package components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import compose.icons.TablerIcons
import compose.icons.tablericons.CameraPlus
import imageService.ImageOptions
import imageService.generateProcessedImagePreview
import imageService.processImage
import imageService.uploadImage
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import printService.ImageElement
import printService.PrintRequest
import ui.DropdownMenu
import ui.SwitchGroup
import utils.getContext
import utils.getFileLoader
import utils.showToast
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.roundToInt

val HEIGHT_RANGE = 50f..2000f
val FILTERS = listOf("No filter", "Contrast", "Contour", "Emboss", "Smooth", "Smooth more", "Edge enhance", "Edge enhance more", "Find edges", "Sharpen")
const val MAX_SIZE = 384

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun ImageForm(onValueChange: (el: PrintRequest) -> Unit) {
    var showFilePicker by remember { mutableStateOf(false) }
    var keepAspectRatio by remember { mutableStateOf(true) }
    var customHeight by remember { mutableStateOf(MAX_SIZE.toFloat()) }
    var rotateRight by remember { mutableStateOf(false) }
    var filterIndex by remember { mutableStateOf(0) }
    var invert by remember { mutableStateOf(false) }

    var uploadedUuid by remember { mutableStateOf<String?>(null) }
    var imagePreview by remember { mutableStateOf<ImageBitmap?>(null) }
    var isUploadLoading by remember { mutableStateOf(false) }
    var isPreviewLoading by remember { mutableStateOf(false) }

    val fileType = listOf("jpg", "png")
    val context = getContext()
    val coroutineScope = rememberCoroutineScope()
    val loadFile = getFileLoader()

    val painter = BitmapPainter(imagePreview ?: ImageBitmap(1, 1))

    LaunchedEffect(Unit) {
        onValueChange(PrintRequest())
    }

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
            val options = ImageOptions(if (rotateRight) 90 else 0, keepAspectRatio, customHeight, FILTERS[filterIndex].lowercase(), invert)
            val response = processImage(uploadedUuid!!, options)
            imagePreview = generateProcessedImagePreview(response)

            val elements = response.chunksBase64.map { ImageElement(it) }.reversed()
            onValueChange(PrintRequest(elements, true))
        } catch(e: Exception) {
            println(e)
            showToast(context, "Error while processing image")
            uploadedUuid = null // reset state
        }

        isPreviewLoading = false
    }

    LaunchedEffect(keepAspectRatio, uploadedUuid, customHeight, rotateRight, filterIndex, invert) {
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
        Modifier.fillMaxHeight().padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isUploadLoading) {
            Box(Modifier.fillMaxHeight().padding(bottom = 150.dp), Alignment.Center) {
                CircularProgressIndicator()
            }
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

        Box(
            Modifier.fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp), RoundedCornerShape(20.dp))
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
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

        Spacer(Modifier.height(15.dp))
        FilledTonalButton(onClick = { showFilePicker = true }) {
            Text("Change image")
        }
        Spacer(Modifier.height(15.dp))
        SwitchGroup(rotateRight, { v -> rotateRight = v }, { Text("Rotate 90 deg") })
        SwitchGroup(keepAspectRatio, { v -> keepAspectRatio = v }, { Text("Keep aspect ratio") })
        if (!keepAspectRatio) {
            Slider(customHeight, { v -> customHeight = v }, valueRange = HEIGHT_RANGE, steps = 100)
            Text("Height: ${customHeight.roundToInt()}", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
        }
        SwitchGroup(invert, { v -> invert = v }, { Text("Invert") })
        Spacer(Modifier.height(5.dp))
        DropdownMenu(filterIndex, { v -> filterIndex = v }, FILTERS, { Text("Filter") }, Modifier.fillMaxWidth())
    }
}

