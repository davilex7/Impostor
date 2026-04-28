package com.davilex.impostor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davilex.impostor.ui.theme.AvatarPalette
import kotlin.math.absoluteValue

fun colorForName(name: String): Color {
    val idx = name.hashCode().absoluteValue % AvatarPalette.size
    return AvatarPalette[idx]
}

fun colorAssignments(names: List<String>): Map<String, Color> {
    val taken = mutableSetOf<Int>()
    val map = mutableMapOf<String, Color>()
    for (name in names) {
        if (name in map) continue
        val preferred = name.hashCode().absoluteValue % AvatarPalette.size
        var idx = preferred
        var attempts = 0
        while (idx in taken && attempts < AvatarPalette.size) {
            idx = (idx + 1) % AvatarPalette.size
            attempts++
        }
        taken += idx
        map[name] = AvatarPalette[idx]
    }
    return map
}

@Composable
fun PlayerAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Int = 48,
    color: Color = colorForName(name)
) {
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size / 2.4).sp,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
        )
    }
}
