package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.KeyboardTheme

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

@Composable
fun ShinaTheme(
    theme: KeyboardTheme = KeyboardTheme.EMERALD_GILGIT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        KeyboardTheme.EMERALD_GILGIT -> EmeraldColorScheme
        KeyboardTheme.KARAKORAM_SUNSET -> SunsetColorScheme
        KeyboardTheme.INDIGO_SLATE -> IndigoColorScheme
        KeyboardTheme.MINIMAL_DARK -> DarkColorScheme
        KeyboardTheme.GOLD_SILK -> GoldColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
