package com.example.android.notepad

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val light = lightColorScheme(
    primary = Color(0xFF28594D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDAEBE1),
    onPrimaryContainer = Color(0xFF153F34),
    secondary = Color(0xFF4F665B),
    secondaryContainer = Color(0xFFE1EAE1),
    onSecondaryContainer = Color(0xFF253F34),
    background = Color(0xFFF6F4ED),
    onBackground = Color(0xFF202E29),
    surface = Color(0xFFFFFDF8),
    onSurface = Color(0xFF202E29),
    surfaceVariant = Color(0xFFE9EEE7),
    onSurfaceVariant = Color(0xFF4A5B53),
    outline = Color(0xFF6B7C72),
    outlineVariant = Color(0xFFD3DBD2)
)

private val dark = darkColorScheme(
    primary = Color(0xFFA4D3C1),
    onPrimary = Color(0xFF12372C),
    primaryContainer = Color(0xFF264E42),
    onPrimaryContainer = Color(0xFFD2EEE2),
    secondary = Color(0xFFB9CBBF),
    secondaryContainer = Color(0xFF324C40),
    onSecondaryContainer = Color(0xFFDDEBE0),
    background = Color(0xFF151D1A),
    onBackground = Color(0xFFE6EEE8),
    surface = Color(0xFF202A26),
    onSurface = Color(0xFFE6EEE8),
    surfaceVariant = Color(0xFF283630),
    onSurfaceVariant = Color(0xFFB8C8BF),
    outline = Color(0xFF85998F),
    outlineVariant = Color(0xFF43564D)
)

@Composable
internal fun NotePadTheme(useDark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (useDark) dark else light, content = content)
}

internal fun Paper.editorColor(darkTheme: Boolean): Color = if (darkTheme) {
    when (this) {
        Paper.PLAIN -> Color(0xFF242B2A)
        Paper.WARM -> Color(0xFF342E24)
        Paper.MINT -> Color(0xFF23372E)
    }
} else {
    when (this) {
        Paper.PLAIN -> Color(0xFFFFFEFB)
        Paper.WARM -> Color(0xFFFFF3D9)
        Paper.MINT -> Color(0xFFE7F2E8)
    }
}
