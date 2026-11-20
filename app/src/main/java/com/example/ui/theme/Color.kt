package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// FriendZone Signature Palette — Pure Crisp Light + Deep Obsidian Dark + Purple-Pink Gradient Accents
val FzPurple = Color(0xFF7C3AED)
val FzPurpleDark = Color(0xFF6D28D9)
val FzPurpleLight = Color(0xFFA78BFA)
val FzPink = Color(0xFFEC4899)
val FzPinkHot = Color(0xFFF43F5E)
val FzPinkLight = Color(0xFFF472B6)
val FzCoral = Color(0xFFFB7185)
val FzAmber = Color(0xFFF59E0B)

// Light Theme Tokens
val LightBackground = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF6F4FC)
val LightSurfaceContainer = Color(0xFFFAF9FE)
val LightOnBackground = Color(0xFF12101B)
val LightOnSurface = Color(0xFF171424)
val LightOnSurfaceVariant = Color(0xFF6B6580)
val LightOutline = Color(0xFFE7E2F3)
val LightOutlineVariant = Color(0xFFF0ECF9)
val LightPrimaryContainer = Color(0xFFF3ECFF)
val LightOnPrimaryContainer = Color(0xFF4C1D95)
val LightSecondaryContainer = Color(0xFFFCE7F3)
val LightOnSecondaryContainer = Color(0xFF831843)

// Dark Theme Tokens
val DarkBackground = Color(0xFF0C0A14)
val DarkSurface = Color(0xFF141122)
val DarkSurfaceVariant = Color(0xFF1E1932)
val DarkSurfaceContainer = Color(0xFF181429)
val DarkOnBackground = Color(0xFFF7F5FF)
val DarkOnSurface = Color(0xFFF3F0FF)
val DarkOnSurfaceVariant = Color(0xFFA9A2C2)
val DarkOutline = Color(0xFF2D2745)
val DarkOutlineVariant = Color(0xFF231E38)
val DarkPrimaryContainer = Color(0xFF2E1A5E)
val DarkOnPrimaryContainer = Color(0xFFE9DDFF)
val DarkSecondaryContainer = Color(0xFF4A1533)
val DarkOnSecondaryContainer = Color(0xFFFFD8EB)

val VerifiedBlue = Color(0xFF3B82F6)
val SuccessGreen = Color(0xFF10B981)
val ErrorRed = Color(0xFFEF4444)

object FriendZoneGradients {
    val Primary = Brush.linearGradient(
        colors = listOf(FzPurple, FzPink)
    )

    val StoryRing = Brush.sweepGradient(
        colors = listOf(FzPurple, FzPink, FzAmber, FzPinkHot, FzPurple)
    )

    val SubtleCardGlow = Brush.linearGradient(
        colors = listOf(
            FzPurple.copy(alpha = 0.08f),
            FzPink.copy(alpha = 0.06f)
        )
    )

    val AvatarPalettes: List<List<Color>> = listOf(
        listOf(Color(0xFF7C3AED), Color(0xFFEC4899)),
        listOf(Color(0xFF6366F1), Color(0xFFA855F7)),
        listOf(Color(0xFFEC4899), Color(0xFFF97316)),
        listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4)),
        listOf(Color(0xFFD946EF), Color(0xFFF43F5E)),
        listOf(Color(0xFF4F46E5), Color(0xFFEC4899)),
    )
}
