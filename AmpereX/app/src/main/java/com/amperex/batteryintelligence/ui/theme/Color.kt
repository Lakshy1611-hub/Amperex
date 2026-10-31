package com.amperex.batteryintelligence.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Premium neutral surfaces with a restrained teal/indigo accent system.
val AmoledBlack = Color(0xFF0B1017)
val GraphiteSurface = Color(0xFF121A24)
val GraphiteCard = Color(0xFF182330)
val GraphiteCardBorder = Color(0xFF263747)

val ElectricLime = Color(0xFF55D6C2)
val MintAccent = Color(0xFF42C9A5)
val ElectricLimeDim = Color(0x3355D6C2)
val VioletAccent = Color(0xFF8C83F6)
val BlueAccent = Color(0xFF669BFA)
val PinkAccent = Color(0xFFE58BB7)

val AmberWarning = Color(0xFFF2B85B)
val CrimsonAlert = Color(0xFFEF7182)

val TextPrimary = Color(0xFFF2F5F8)
val TextSecondary = Color(0xFFADB9C6)
val TextMuted = Color(0xFF81909F)

val DarkColorScheme = darkColorScheme(
    primary = ElectricLime,
    onPrimary = Color(0xFF06211D),
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
    primary = Color(0xFF087F78),
    onPrimary = Color.White,
    secondary = Color(0xFF4C5FC7),
    onSecondary = Color.White,
    tertiary = Color(0xFF7257C8),
    background = Color(0xFFF3F6F8),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE9EFF2),
    onSurface = Color(0xFF17252C),
    onSurfaceVariant = Color(0xFF5E7078),
    outline = Color(0xFFD8E1E5),
    error = Color(0xFFC04455)
)
