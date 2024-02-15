package printService

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.pow

@Serializable
sealed class PrintElement {
    abstract val printType: Int
    val encodeType = 0
}

private fun boolToInt(b: Boolean): Int = if (b) {
    1
} else {
    0
}

@OptIn(ExperimentalEncodingApi::class, ExperimentalSerializationApi::class)
@Serializable
data class TextElement(
    @SerialName("basetext")
    private val encodedText: String,
    @SerialName("fontSize")
    private val fontSize: Int,
    @SerialName("bold")
    private val boldInt: Int,
    @SerialName("underline")
    private val underlineInt: Int,
    override val printType: Int = 1
) : PrintElement() {
    constructor(text: String, big: Boolean, bold: Boolean, underline: Boolean) : this(
        encodedText = Base64.encode(text.encodeToByteArray()),
        fontSize = 1 + boolToInt(big),
        boldInt = boolToInt(bold),
        underlineInt = boolToInt(underline),
    )
}

@OptIn(ExperimentalEncodingApi::class)
@Serializable
data class ImageElement(
    @SerialName("basetext")
    val encodedImage: String,
    override val printType: Int = 5
) : PrintElement()

@Serializable
data class DocumentContent(val textList: List<PrintElement>)

@Serializable
data class PrintDocument(
    @Transient
    private val elements: List<PrintElement> = emptyList(),
    val printID: Int = (1..10.toDouble().pow(6).toInt()).random(),
    val command: Int = 3,
    val encryptFlag: Int = 0,
    val hasHead: Int = 0,
    val hasSignature: Int = 0,
    val hasTail: Int = 0,
    val isFromDirectPrint: Boolean = false,
    val msgType: Int = 1,
    val pkgCount: Int = 1,
    val pkgNo: Int = 1,
    val priority: Int = 0,
    val result: Int = 0,
    val scripType: Int = 3,
    val content: DocumentContent = DocumentContent(elements)
) {
    companion object {
        val converter = Json { encodeDefaults = true }
    }

    val json: String
        get() = converter.encodeToString(this)
}

@Serializable
data class PrintRequest(val elements: List<PrintElement>, val separate: Boolean = false) {
    suspend fun print(service: PrintService) {
        if (separate) {
            for ((i, element) in elements.withIndex()) {
                service.print(PrintDocument(listOf(element), priority = i))
            }
        } else {
            service.print(PrintDocument(elements))
        }
    }
}