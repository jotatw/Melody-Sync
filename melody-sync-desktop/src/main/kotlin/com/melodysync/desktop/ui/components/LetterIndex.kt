package com.melodysync.desktop.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LetterIndex(
    availableLetters: Set<Char>,
    currentLetter: Char?,
    onLetterSelected: (Char) -> Unit,
) {
    val alphabet = ('A'..'Z')

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(24.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        alphabet.forEach { letter ->
            val available = letter in availableLetters
            val active = letter == currentLetter

            Text(
                text = letter.toString(),
                fontSize = 10.sp,
                color = when {
                    active -> MaterialTheme.colorScheme.primary
                    available -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                },
                modifier = Modifier
                    .width(20.dp)
                    .clickable(enabled = available) { onLetterSelected(letter) },
            )
        }
    }
}
