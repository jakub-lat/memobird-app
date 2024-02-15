package components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Refresh
import imageService.generateProcessedImagePreview
import imageService.processImage
import imageService.uploadImage
import kotlinx.coroutines.launch
import printService.ImageElement
import printService.PrintRequest
import qrcode.QRCode
import qrcode.color.Colors
import utils.getContext
import utils.showToast

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun QRCodeForm(onValueChange: (request: PrintRequest) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val context = getContext()
    val keyboardController = LocalSoftwareKeyboardController.current

    var data by remember { mutableStateOf("") }
    var qrCodeBytes by remember { mutableStateOf(byteArrayOf()) }
    var imagePreview by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isDirty by remember { mutableStateOf(false) }

    val painter = BitmapPainter(imagePreview ?: ImageBitmap(1, 1))

    LaunchedEffect(Unit) {
        onValueChange(PrintRequest())
    }

    fun generate() = coroutineScope.launch {
        isLoading = true
        try {
            keyboardController?.hide()

            val result = QRCode.ofSquares()
                .withColor(Colors.BLACK)
                .withSize(20)
                .withInnerSpacing(0)
                .build(data)

            qrCodeBytes = result.renderToBytes()

            val uuid = uploadImage(qrCodeBytes)
            val response = processImage(uuid)
            imagePreview = generateProcessedImagePreview(response)

            onValueChange(PrintRequest(listOf(ImageElement(response.chunksBase64.first()))))
            isDirty = false
        } catch (e: Exception) {
            showToast(context, "Error: ${e.message}")
            println(e)
        }
        isLoading = false
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (imagePreview != null) {
                Image(
                    painter,
                    contentDescription = "QR code",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Icon(
                    Icons.Default.QrCode2,
                    "placeholder",
                    Modifier.size(200.dp),
                    tint = Color.Black.copy(alpha = 0.1f)
                )
            }
        }
        Spacer(Modifier.height(15.dp))
        OutlinedTextField(
            value = data,
            onValueChange = { v ->
                data = v
                isDirty = true
            },
            label = { Text("Data") },
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            trailingIcon = {
                if (!isDirty) return@OutlinedTextField

                IconButton({ generate() }, enabled = !isLoading) {
                    Icon(TablerIcons.Refresh, "apply")
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { generate() }, onGo = { generate() })
        )
    }
}