package com.hypernexus.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val InterFontFamily = FontFamily.SansSerif

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.Bold,
        fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = (-0.5).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = (-0.3).sp
    ),
    titleLarge = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp, lineHeight = 28.sp, letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    ),
    labelSmall = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp
    )
)
