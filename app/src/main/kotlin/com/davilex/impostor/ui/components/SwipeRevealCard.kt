package com.davilex.impostor.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun SwipeRevealCard(
    revealed: @Composable () -> Unit,
    onRevealed: () -> Unit,
    modifier: Modifier = Modifier,
    coverHint: String = "Arrastra tu tarjeta para revelar tu palabra. ¡Evita a los impostores! 👀"
) {
    var heightPx by remember { mutableFloatStateOf(0f) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var isRevealed by remember { mutableStateOf(false) }

    val animatedOffset by animateFloatAsState(
        targetValue = if (isRevealed) heightPx else dragOffset,
        animationSpec = tween(durationMillis = 320),
        label = "swipe-reveal-offset"
    )

    LaunchedEffect(Unit) {
        snapshotFlow { isRevealed }.collect { revealed ->
            if (revealed) onRevealed()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { heightPx = it.height.toFloat() }
            .clip(RoundedCornerShape(24.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            revealed()
        }

        if (!isRevealed) {
            CoverCard(
                hint = coverHint,
                offsetY = if (heightPx > 0f) animatedOffset else dragOffset,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(heightPx) {
                        if (heightPx <= 0f) return@pointerInput
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (dragOffset >= heightPx * 0.5f) {
                                    isRevealed = true
                                } else {
                                    dragOffset = 0f
                                }
                            },
                            onDragCancel = { dragOffset = 0f }
                        ) { _, dragAmount ->
                            val next = (dragOffset + dragAmount).coerceIn(0f, heightPx)
                            dragOffset = next
                        }
                    }
            )
        }
    }
}

@Composable
private fun CoverCard(
    hint: String,
    offsetY: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .offset { IntOffset(0, offsetY.toInt()) }
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF3B2A6E),
                        Color(0xFF1F1640)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "👇",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = hint,
                color = Color.White.copy(alpha = 0.92f),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Desliza hacia abajo ↓",
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
