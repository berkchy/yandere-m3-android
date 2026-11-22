package app.yande.re.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.yande.re.R

val GoogleSansRegular = FontFamily(
    Font(R.font.google_sans_regular, FontWeight.Normal)
)

private val base = Typography()

val AppTypography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = GoogleSansRegular),
    displayMedium = base.displayMedium.copy(fontFamily = GoogleSansRegular),
    displaySmall = base.displaySmall.copy(fontFamily = GoogleSansRegular),
    headlineLarge = base.headlineLarge.copy(fontFamily = GoogleSansRegular),
    headlineMedium = base.headlineMedium.copy(fontFamily = GoogleSansRegular),
    headlineSmall = base.headlineSmall.copy(fontFamily = GoogleSansRegular),
    titleLarge = base.titleLarge.copy(fontFamily = GoogleSansRegular),
    titleMedium = base.titleMedium.copy(fontFamily = GoogleSansRegular),
    titleSmall = base.titleSmall.copy(fontFamily = GoogleSansRegular),
    bodyLarge = base.bodyLarge.copy(fontFamily = GoogleSansRegular),
    bodyMedium = base.bodyMedium.copy(fontFamily = GoogleSansRegular),
    bodySmall = base.bodySmall.copy(fontFamily = GoogleSansRegular),
    labelLarge = base.labelLarge.copy(fontFamily = GoogleSansRegular),
    labelMedium = base.labelMedium.copy(fontFamily = GoogleSansRegular),
    labelSmall = base.labelSmall.copy(fontFamily = GoogleSansRegular)
)
