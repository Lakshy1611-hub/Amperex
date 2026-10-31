package com.amperex.batteryintelligence.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// AMOLED Black & Graphite Palette
val AmoledBlack = Color(0xFF000000)
val GraphiteSurface = Color(0xFF121214)
val GraphiteCard = Color(0xFF1C1C1F)
val GraphiteCardBorder = Color(0xFF28282D)

// Brand Accents
val ElectricLime = Color(0xFFCCFF00)
val MintAccent = Color(0xFF00F0A8)
val ElectricLimeDim = Color(0x33CCFF00)

// Status & Warnings
val AmberWarning = Color(0xFFFFB800)
val CrimsonAlert = Color(0xFFFF4444)

// Text Colors
val TextPrimary = Color(0xFFF4F4F6)
val TextSecondary = Color(0xFF9E9EA7)
val TextMuted = Color(0xFF656570)

val DarkColorScheme = darkColorScheme(
    primary = ElectricLime,
    onPrimary = Color.Black,
    secondary = MintAccent,
    onSecondary = Color.Black,
    tertiary = AmberWarning,
    background = AmoledBlack,
    surface = GraphiteSurface,
    surfaceVariant = GraphiteCard,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = GraphiteCardBorder,
    error = CrimsonAlert
)

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4A8500),
    onPrimary = Color.White,
    secondary = Color(0xFF008960),
    onSecondary = Color.White,
    tertiary = Color(0xFFB87800),
    background = Color(0xFFF8F9FA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF0F1F5),
    onSurface = Color(0xFF1A1A1E),
    onSurfaceVariant = Color(0xFF6B6B76),
    outline = Color(0xFFE2E4E9),
    error = Color(0xFFD32F2F)
)
