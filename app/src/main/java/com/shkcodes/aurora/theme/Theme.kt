@file:Suppress("MagicNumber", "ConflictingOnColor")

package com.shkcodes.aurora.theme

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.shkcodes.aurora.R

val typography: Typography
    @Composable
    get() = MaterialTheme.typography

val colors: Colors
    @Composable
    get() = MaterialTheme.colors

private val fontFamily = FontFamily(
    Font(R.font.montserrat_thin, weight = FontWeight.Thin),
    Font(R.font.montserrat_extralight, weight = FontWeight.ExtraLight),
    Font(R.font.montserrat_light, weight = FontWeight.Light),
    Font(R.font.montserrat_regular, weight = FontWeight.Normal),
    Font(R.font.montserrat_medium, weight = FontWeight.Medium),
    Font(R.font.montserrat_semibold, weight = FontWeight.SemiBold),
    Font(R.font.montserrat_bold, weight = FontWeight.Bold),
)

private val themeTypography = Typography(defaultFontFamily = fontFamily)

private val colorPalette = darkColors(
    primary = Color(0xFF0DE791),
    primaryVariant = Color(0xFF0AAF6E),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFFFFC107),
    onSecondary = Color(0xFF0DE791),
    surface = Color(0xFF212121),
    onSurface = Color(0xFF0DE791),
    background = Color(0xFF212121),
    onBackground = Color(0xFFFFFFFF),
    error = Color(0xFFF56460),
    onError = Color(0xFFFFFFFF)
)

@Composable
fun AuroraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = colorPalette,
        content = content,
        typography = themeTypography
    )
}

@Composable
internal fun ThemedPreview(
    content: @Composable () -> Unit
) {
    AuroraTheme {
        Scaffold {
            content()
        }
    }
}
