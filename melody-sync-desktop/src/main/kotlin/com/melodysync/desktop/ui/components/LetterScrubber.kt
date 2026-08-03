package com.melodysync.desktop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.theme.Widths
import kotlin.math.roundToInt

/**
 * Letter scrubber — a thin scrollbar that follows the list and shows the
 * letter currently being viewed. Dragging the track scrubs through the
 * available letters. Replaces the A-Z index column so the list order is
 * never broken by a fixed letter gutter.
 */
@Composable
fun LetterScrubber(
    availableLetters: Set<Char>,
    currentLetter: Char?,
    onLetterSelected: (Char) -> Unit,
) {
    val letters = remember(availableLetters) { availableLetters.sorted() }
    val letterIndex = letters.indexOf(currentLetter)
    val fraction = if (letters.size > 1 && letterIndex >= 0) {
        letterIndex.toFloat() / letters.lastIndex.toFloat()
    } else {
        0f
    }

    val density = LocalDensity.current
    val thumbSizePx = with(density) { 24.dp.toPx() }
    var trackHeightPx by remember { mutableFloatStateOf(0f) }
    val thumbOffsetPx = if (trackHeightPx > thumbSizePx) {
        fraction * (trackHeightPx - thumbSizePx)
    } else {
        0f
    }

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(Widths.letterScrubber)
            .onSizeChanged { trackHeightPx = it.height.toFloat() }
            .pointerInput(letters) {
                detectDragGestures(
                    onDrag = { change, _ ->
                        change.consume()
                        if (letters.size > 1) {
                            val frac = (change.position.y / trackHeightPx).coerceIn(0f, 1f)
                            val index = (frac * (letters.size - 1)).roundToInt()
                                .coerceIn(0, letters.lastIndex)
                            onLetterSelected(letters[index])
                        }
                    },
                )
            },
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(4.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset { IntOffset(0, thumbOffsetPx.roundToInt()) }
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            if (letterIndex >= 0) {
                Text(
                    currentLetter.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}
