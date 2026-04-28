package com.davilex.impostor.ui.components

import androidx.compose.animation.core.Animatable
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.launch

@Composable
fun SwipeRevealCard(
    revealed: @Composable () -> Unit,
    onRevealed: () -> Unit,
    modifier: Modifier = Modifier,
    coverHint: String = "Desliza tu tarjeta hacia arriba para revelar tu palabra. ¡Evita a los impostores! 👀"
) {
    var heightPx by remember { mutableFloatStateOf(0f) }
    val offset = remember { Animatable(0f) }
    var isRevealed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isRevealed) {
        if (isRevealed) onRevealed()
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
                offsetY = offset.value,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(heightPx) {
                        if (heightPx <= 0f) return@pointerInput
                        detectVerticalDragGestures(
                            onDragEnd = {
                                scope.launch {
                                    if (offset.value <= -heightPx * 0.5f) {
                                        offset.animateTo(
                                            targetValue = -heightPx,
                                            animationSpec = tween(durationMillis = 280)
                                        )
                                        isRevealed = true
                                    } else {
                                        offset.animateTo(
                                            targetValue = 0f,
                                            animationSpec = tween(durationMillis = 220)
                                        )
                                    }
                                }
                            },
                            onDragCancel = {
                                scope.launch {
                                    offset.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(durationMillis = 220)
                                    )
                                }
                            }
                        ) { _, dragAmount ->
                            val next = (offset.value + dragAmount).coerceIn(-heightPx, 0f)
                            scope.launch { offset.snapTo(next) }
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
                text = "👆",
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
                text = "Desliza hacia arriba ↑",
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
