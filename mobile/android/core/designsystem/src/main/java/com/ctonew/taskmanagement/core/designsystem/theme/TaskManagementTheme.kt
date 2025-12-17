package com.ctonew.taskmanagement.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ctonew.taskmanagement.core.designsystem.R

private val GeistSans = FontFamily(
  Font(resId = R.font.geist_regular, weight = FontWeight.Normal),
  Font(resId = R.font.geist_medium, weight = FontWeight.Medium),
  Font(resId = R.font.geist_semibold, weight = FontWeight.SemiBold),
  Font(resId = R.font.geist_bold, weight = FontWeight.Bold),
)

private val GeistMono = FontFamily(
  Font(resId = R.font.geist_mono_regular, weight = FontWeight.Normal),
)

private val TaskManagementTypography = Typography(
  displayLarge = TextStyle(
    fontFamily = GeistSans,
    fontWeight = FontWeight.SemiBold,
    fontSize = 36.sp,
    lineHeight = 44.sp,
  ),
  displayMedium = TextStyle(
    fontFamily = GeistSans,
    fontWeight = FontWeight.SemiBold,
    fontSize = 30.sp,
    lineHeight = 38.sp,
  ),
  headlineLarge = TextStyle(
    fontFamily = GeistSans,
    fontWeight = FontWeight.SemiBold,
    fontSize = 24.sp,
    lineHeight = 32.sp,
  ),
  titleLarge = TextStyle(
    fontFamily = GeistSans,
    fontWeight = FontWeight.Medium,
    fontSize = 20.sp,
    lineHeight = 28.sp,
  ),
  titleMedium = TextStyle(
    fontFamily = GeistSans,
    fontWeight = FontWeight.Medium,
    fontSize = 18.sp,
    lineHeight = 26.sp,
  ),
  bodyLarge = TextStyle(
    fontFamily = GeistSans,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
  ),
  bodyMedium = TextStyle(
    fontFamily = GeistSans,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
  ),
  labelLarge = TextStyle(
    fontFamily = GeistSans,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
  ),
  labelMedium = TextStyle(
    fontFamily = GeistSans,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
  ),
  labelSmall = TextStyle(
    fontFamily = GeistMono,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
  ),
)

private val TaskManagementShapes = Shapes(
  extraSmall = MaterialShape.Rounded4,
  small = MaterialShape.Rounded8,
  medium = MaterialShape.Rounded12,
  large = MaterialShape.Rounded16,
  extraLarge = MaterialShape.Rounded24,
)

private object MaterialShape {
  val Rounded4: CornerBasedShape = RoundedCornerShape(4.dp)
  val Rounded8: CornerBasedShape = RoundedCornerShape(8.dp)
  val Rounded12: CornerBasedShape = RoundedCornerShape(12.dp)
  val Rounded16: CornerBasedShape = RoundedCornerShape(16.dp)
  val Rounded24: CornerBasedShape = RoundedCornerShape(24.dp)
}

@Composable
private fun taskManagementColorScheme(darkTheme: Boolean): ColorScheme {
  val primary = colorResource(id = R.color.tm_primary)
  val onPrimary = colorResource(id = R.color.tm_onPrimary)
  val secondary = colorResource(id = R.color.tm_secondary)
  val onSecondary = colorResource(id = R.color.tm_onSecondary)
  val background = colorResource(id = R.color.tm_background)
  val onBackground = colorResource(id = R.color.tm_onBackground)
  val surface = colorResource(id = R.color.tm_surface)
  val onSurface = colorResource(id = R.color.tm_onSurface)
  val outline = colorResource(id = R.color.tm_outline)

  val surfaceVariant = colorResource(id = R.color.background_tertiary)
  val onSurfaceVariant = colorResource(id = R.color.foreground_secondary)

  return if (darkTheme) {
    darkColorScheme(
      primary = primary,
      onPrimary = onPrimary,
      secondary = secondary,
      onSecondary = onSecondary,
      background = background,
      onBackground = onBackground,
      surface = surface,
      onSurface = onSurface,
      surfaceVariant = surfaceVariant,
      onSurfaceVariant = onSurfaceVariant,
      outline = outline,
    )
  } else {
    lightColorScheme(
      primary = primary,
      onPrimary = onPrimary,
      secondary = secondary,
      onSecondary = onSecondary,
      background = background,
      onBackground = onBackground,
      surface = surface,
      onSurface = onSurface,
      surfaceVariant = surfaceVariant,
      onSurfaceVariant = onSurfaceVariant,
      outline = outline,
    )
  }
}

@Composable
fun TaskManagementTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = taskManagementColorScheme(darkTheme),
    typography = TaskManagementTypography,
    shapes = TaskManagementShapes,
    content = content,
  )
}
