package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.KeyboardTheme

// Light Schemes
private val EmeraldColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    secondary = EmeraldSecondary,
    background = EmeraldBackground,
    surface = EmeraldSurface,
    onPrimary = Color.White,
    onBackground = EmeraldKeyText,
    onSurface = EmeraldKeyText
)

private val SunsetColorScheme = lightColorScheme(
    primary = SunsetPrimary,
    secondary = SunsetSecondary,
    background = SunsetBackground,
    surface = SunsetSurface,
    onPrimary = Color.White,
    onBackground = SunsetKeyText,
    onSurface = SunsetKeyText
)

private val IndigoColorScheme = darkColorScheme(
    primary = IndigoPrimary,
    secondary = IndigoSecondary,
    background = IndigoBackground,
    surface = IndigoSurface,
    onPrimary = Color.White,
    onBackground = IndigoKeyText,
    onSurface = IndigoKeyText
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.Black,
    onBackground = DarkKeyText,
    onSurface = DarkKeyText
)

private val GoldColorScheme = lightColorScheme(
    primary = GoldPrimary,
    secondary = GoldSecondary,
    background = GoldBackground,
    surface = GoldSurface,
    onPrimary = Color.White,
    onBackground = GoldKeyText,
    onSurface = GoldKeyText
)

// High-Contrast Dark Color Schemes for Low-Light Visibility
private val EmeraldDarkColorScheme = darkColorScheme(
    primary = Color(0xFF4EE3A5),
    secondary = Color(0xFF7DD4B2),
    tertiary = Color(0xFF80D8FF),
    background = Color(0xFF0F1412),
    surface = Color(0xFF18221D),
    surfaceVariant = Color(0xFF22302A),
    onPrimary = Color(0xFF003827),
    onSecondary = Color(0xFF003827),
    onBackground = Color(0xFFE4ECE8),
    onSurface = Color(0xFFE4ECE8),
    onSurfaceVariant = Color(0xFFC0CFC7),
    primaryContainer = Color(0xFF00513B),
    onPrimaryContainer = Color(0xFFA6F6D4),
    secondaryContainer = Color(0xFF28483B),
    onSecondaryContainer = Color(0xFFC8EADA),
    tertiaryContainer = Color(0xFF004D65),
    onTertiaryContainer = Color(0xFFBAE9FF)
)

private val SunsetDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF8B5E),
    secondary = Color(0xFFE5A696),
    tertiary = Color(0xFFFFD54F),
    background = Color(0xFF15100E),
    surface = Color(0xFF211A17),
    surfaceVariant = Color(0xFF322723),
    onPrimary = Color(0xFF5E1B00),
    onBackground = Color(0xFFFFECE5),
    onSurface = Color(0xFFFFECE5),
    onSurfaceVariant = Color(0xFFD8C2BB),
    primaryContainer = Color(0xFF852B00),
    onPrimaryContainer = Color(0xFFFFDBCF)
)

private val IndigoDarkColorScheme = darkColorScheme(
    primary = Color(0xFF9FA8DA),
    secondary = Color(0xFFC5CAE9),
    tertiary = Color(0xFF80DEEA),
    background = Color(0xFF0D0F1A),
    surface = Color(0xFF171B2D),
    surfaceVariant = Color(0xFF22273F),
    onPrimary = Color(0xFF1A237E),
    onBackground = Color(0xFFE8EAF6),
    onSurface = Color(0xFFE8EAF6),
    onSurfaceVariant = Color(0xFFC5CAE9),
    primaryContainer = Color(0xFF283593),
    onPrimaryContainer = Color(0xFFE8EAF6)
)

private val HighContrastDarkColorScheme = darkColorScheme(
    primary = Color(0xFF80D8FF),
    secondary = Color(0xFF80CBC4),
    tertiary = Color(0xFFFFD54F),
    background = Color(0xFF0A0A0A),
    surface = Color(0xFF161616),
    surfaceVariant = Color(0xFF242424),
    onPrimary = Color(0xFF003648),
    onSecondary = Color(0xFF003833),
    onBackground = Color(0xFFF5F5F5),
    onSurface = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFFD0D0D0),
    primaryContainer = Color(0xFF004D65),
    onPrimaryContainer = Color(0xFFBCE9FF),
    secondaryContainer = Color(0xFF004F48),
    onSecondaryContainer = Color(0xFFB2DFDB)
)

private val GoldDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFD54F),
    secondary = Color(0xFFE0D08A),
    tertiary = Color(0xFF80CBC4),
    background = Color(0xFF14120B),
    surface = Color(0xFF201D13),
    surfaceVariant = Color(0xFF302B1D),
    onPrimary = Color(0xFF483A00),
    onBackground = Color(0xFFFFF9E6),
    onSurface = Color(0xFFFFF9E6),
    onSurfaceVariant = Color(0xFFDDD2B2),
    primaryContainer = Color(0xFF6B5500),
    onPrimaryContainer = Color(0xFFFFEFA6)
)

@Composable
fun ShinaTheme(
    theme: KeyboardTheme = KeyboardTheme.EMERALD_GILGIT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        when (theme) {
            KeyboardTheme.EMERALD_GILGIT -> EmeraldDarkColorScheme
            KeyboardTheme.KARAKORAM_SUNSET -> SunsetDarkColorScheme
            KeyboardTheme.INDIGO_SLATE -> IndigoDarkColorScheme
            KeyboardTheme.MINIMAL_DARK -> HighContrastDarkColorScheme
            KeyboardTheme.GOLD_SILK -> GoldDarkColorScheme
        }
    } else {
        when (theme) {
            KeyboardTheme.EMERALD_GILGIT -> EmeraldColorScheme
            KeyboardTheme.KARAKORAM_SUNSET -> SunsetColorScheme
            KeyboardTheme.INDIGO_SLATE -> IndigoColorScheme
            KeyboardTheme.MINIMAL_DARK -> DarkColorScheme
            KeyboardTheme.GOLD_SILK -> GoldColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
