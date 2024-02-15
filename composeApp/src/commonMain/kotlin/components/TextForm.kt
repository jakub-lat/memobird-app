package components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.OutlinedRichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import printService.PrintElement
import printService.PrintRequest
import printService.TextElement
import ui.RichTextStyleRow

val BigFontSize = 28.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextForm(onValueChange: (request: PrintRequest) -> Unit) {
    val state = rememberRichTextState()

    LaunchedEffect(Unit) {
        onValueChange(PrintRequest())
    }

    LaunchedEffect(state.annotatedString) {
        val elements = richTextToPrintElements(state)
        onValueChange(PrintRequest(elements))
    }

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        OutlinedRichTextEditor(
            state = state,
            modifier = Modifier.fillMaxWidth(),
            minLines = 5,
            maxLines = 10,
            label = { Text("Enter text..") },
            shape = RoundedCornerShape(15.dp)
        )
        RichTextStyleRow(
            state = state,
            modifier = Modifier.fillMaxWidth(),
            bigFontSize = BigFontSize
        )
    }
}

fun richTextToPrintElements(state: RichTextState): List<PrintElement> {
    val cutPoints = state.annotatedString.spanStyles.flatMap { listOf(it.start, it.end) }
    val paragraphEnds = state.annotatedString.paragraphStyles.map { it.end }
    val fragments = cutPoints.sortedBy { it }.zipWithNext()

    val result = fragments.withIndex().map { (index, fragment) ->
        val (start, end) = fragment

        var text = state.annotatedString.text.substring(start, end)

        val isNewline = paragraphEnds.contains(end)
        val isLast = index == fragments.size - 1
        if (isNewline || isLast) text += "\n"

        val styles = state.annotatedString.spanStyles.filter { it.start <= start && it.end >= end }
        val isBold = styles.any { it.item.fontWeight == FontWeight.Bold }
        val isBig = styles.any { it.item.fontSize == BigFontSize }
        val isUnderline = styles.any { it.item.textDecoration == TextDecoration.Underline }

        TextElement(text, isBig, isBold, isUnderline)
    }

    return result
}