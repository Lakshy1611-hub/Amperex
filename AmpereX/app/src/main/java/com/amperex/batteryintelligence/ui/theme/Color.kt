package com.amperex.batteryintelligence.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// AmpereX signature palette: midnight navy, vivid cyan, violet and mint.
val AmoledBlack = Color(0xFF080B16)
val GraphiteSurface = Color(0xFF11182A)
val GraphiteCard = Color(0xFF182238)
val GraphiteCardBorder = Color(0xFF293957)

val ElectricLime = Color(0xFF65E8FF)
val MintAccent = Color(0xFF53E0B2)
val ElectricLimeDim = Color(0x3365E8FF)
val VioletAccent = Color(0xFF9B8CFF)
val BlueAccent = Color(0xFF568BFF)
val PinkAccent = Color(0xFFFF7EB6)

val AmberWarning = Color(0xFFFFC15C)
val CrimsonAlert = Color(0xFFFF667D)

val TextPrimary = Color(0xFFF4F7FF)
val TextSecondary = Color(0xFFAAB6CC)
val TextMuted = Color(0xFF75839D)

val DarkColorScheme = darkColorScheme(
    primary = ElectricLime,
    onPrimary = Color(0xFF06121B),
    secondary = MintAccent,
    onSecondary = Color(0xFF062018),
    tertiary = VioletAccent,
    background = AmoledBlack,
    surface = GraphiteSurface,
    surfaceVariant = GraphiteCard,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = GraphiteCardBorder,
    error = CrimsonAlert
)

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF087F9E),
    onPrimary = Color.White,
    secondary = Color(0xFF087D62),
    onSecondary = Color.White,
    tertiary = Color(0xFF6555C7),
    background = Color(0xFFF4F7FC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEAF0F8),
    onSurface = Color(0xFF172238),
    onSurfaceVariant = Color(0xFF5F6E86),
    outline = Color(0xFFDCE4EF),
    error = Color(0xFFC43E58)
)
