package com.example.onlinequizapp.ui.welcome

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.R
import com.example.onlinequizapp.ui.theme.ThemeManager
import com.example.onlinequizapp.ui.theme.ThemeMode

@Composable
private fun isAppInDarkTheme(): Boolean {
    val themeMode by ThemeManager.themeMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    return when (themeMode) {
        ThemeMode.DARK   -> true
        ThemeMode.LIGHT  -> false
        ThemeMode.SYSTEM -> isSystemDark
    }
}

/**
 * QuizMaster Welcome / Introduction Screen.
 * Shown once upon initial app launch (or data clear, or major app updates).
 */
@Composable
fun WelcomeScreen(
    onGetStartedClick: () -> Unit,
    onLoginClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()

    // Palette tokens
    val bgGradient = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F172A),
                Color(0xFF1E1B4B),
                Color(0xFF0F172A)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF6F8FE),
                Color(0xFFFFFFFF),
                Color(0xFFF1F5F9)
            )
        )
    }

    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.92f) else Color.White.copy(alpha = 0.95f)
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        // Decorative background canvas
        WelcomeBackgroundDecor(isDark = isDark)

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // 1. Top Logo Badge (Graduation Cap in gradient circle)
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .shadow(elevation = 12.dp, shape = CircleShape, ambientColor = Color(0xFF6366F1), spotColor = Color(0xFF4F46E5))
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.School,
                        contentDescription = "QuizMaster Logo",
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Brand Name: "QuizMaster"
                val quizMasterText = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = textPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 36.sp
                        )
                    ) {
                        append("Quiz")
                    }
                    withStyle(
                        style = SpanStyle(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFA855F7))
                            ),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 36.sp
                        )
                    ) {
                        append("Master")
                    }
                }
                Text(
                    text = quizMasterText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Subtitles
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Learn",
                        color = textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "  •  ",
                        color = Color(0xFF6366F1),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Practice",
                        color = textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "  •  ",
                        color = Color(0xFF6366F1),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Improve",
                        color = textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Score tomorrow.",
                    color = textSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Central 3D Illustration
                Image(
                    painter = painterResource(id = R.drawable.welcome_illustration),
                    contentDescription = "Welcome to QuizMaster",
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(230.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 5. Feature Highlights Glass Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = if (isDark) 4.dp else 10.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = Color(0xFF6366F1).copy(alpha = 0.15f),
                            spotColor = Color(0xFF4F46E5).copy(alpha = 0.2f)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp, horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        FeatureHighlightColumn(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.SportsEsports,
                            iconBg = if (isDark) Color(0xFF312E81) else Color(0xFFEEF2FF),
                            iconTint = Color(0xFF6366F1),
                            title = "Interactive\nQuizzes",
                            subtitle = "Make learning\nfun & engaging.",
                            textColor = textPrimary,
                            subtitleColor = textSecondary
                        )

                        // Subtle divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(90.dp)
                                .background(cardBorder.copy(alpha = 0.7f))
                        )

                        FeatureHighlightColumn(
                            modifier = Modifier.weight(1f),
                            icon = Icons.AutoMirrored.Rounded.TrendingUp,
                            iconBg = if (isDark) Color(0xFF064E3B) else Color(0xFFECFDF5),
                            iconTint = Color(0xFF10B981),
                            title = "Real-time\nStats",
                            subtitle = "Track your\nprogress easily.",
                            textColor = textPrimary,
                            subtitleColor = textSecondary
                        )

                        // Subtle divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(90.dp)
                                .background(cardBorder.copy(alpha = 0.7f))
                        )

                        FeatureHighlightColumn(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Star,
                            iconBg = if (isDark) Color(0xFF881337) else Color(0xFFFFF1F2),
                            iconTint = Color(0xFFF43F5E),
                            title = "Build\nYour Skills",
                            subtitle = "Learn more.\nGrow faster.",
                            textColor = textPrimary,
                            subtitleColor = textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 7. Bottom Actions: "Get Started"
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Get Started Pill Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(30.dp),
                            ambientColor = Color(0xFF4F46E5).copy(alpha = 0.35f),
                            spotColor = Color(0xFF4338CA).copy(alpha = 0.45f)
                        )
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF6366F1),
                                    Color(0xFF4F46E5),
                                    Color(0xFF4338CA)
                                )
                            )
                        )
                        .clickable(onClick = onGetStartedClick),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Get Started",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.3.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Reusable column inside the feature highlights card.
 */
@Composable
private fun FeatureHighlightColumn(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    textColor: Color,
    subtitleColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon circular badge
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Title
        Text(
            text = title,
            color = textColor,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Subtitle
        Text(
            text = subtitle,
            color = subtitleColor,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

/**
 * Organic decorative curves, rings, and stars on the background canvas.
 */
@Composable
private fun WelcomeBackgroundDecor(isDark: Boolean) {
    val upperWaveColor = if (isDark) Color(0xFF312E81).copy(alpha = 0.25f) else Color(0xFF818CF8).copy(alpha = 0.35f)
    val upperRightCyan = if (isDark) Color(0xFF0E7490).copy(alpha = 0.2f) else Color(0xFF7DD3FC).copy(alpha = 0.4f)
    val ringColor = if (isDark) Color(0xFF6366F1).copy(alpha = 0.3f) else Color(0xFF93C5FD).copy(alpha = 0.55f)
    val starColor = Color(0xFFFBBF24)
    val bottomWaveColor = if (isDark) Color(0xFF4338CA).copy(alpha = 0.25f) else Color(0xFF6366F1).copy(alpha = 0.4f)
    val bottomCyanColor = if (isDark) Color(0xFF0369A1).copy(alpha = 0.2f) else Color(0xFF38BDF8).copy(alpha = 0.35f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Top-Left organic curved wave
        val pathTopLeft = Path().apply {
            moveTo(0f, 0f)
            lineTo(w * 0.45f, 0f)
            cubicTo(
                w * 0.35f, h * 0.08f,
                w * 0.15f, h * 0.12f,
                0f, h * 0.14f
            )
            close()
        }
        drawPath(path = pathTopLeft, color = upperWaveColor)

        // 2. Top-Right cyan organic glow
        val pathTopRight = Path().apply {
            moveTo(w, 0f)
            lineTo(w * 0.65f, 0f)
            cubicTo(
                w * 0.75f, h * 0.07f,
                w * 0.90f, h * 0.09f,
                w, h * 0.12f
            )
            close()
        }
        drawPath(path = pathTopRight, color = upperRightCyan)

        // 3. Faint decorative ring at upper left
        drawCircle(
            color = ringColor,
            radius = 16f,
            center = Offset(w * 0.08f, h * 0.18f),
            style = Stroke(width = 6f)
        )

        // 4. Little decorative golden star at upper right
        drawCircle(
            color = starColor.copy(alpha = 0.9f),
            radius = 7f,
            center = Offset(w * 0.88f, h * 0.17f)
        )

        // 5. Small blue accent sparkle near middle left
        drawCircle(
            color = ringColor,
            radius = 5f,
            center = Offset(w * 0.12f, h * 0.38f)
        )

        // 6. Small golden sparkle near middle right
        drawCircle(
            color = starColor.copy(alpha = 0.8f),
            radius = 4f,
            center = Offset(w * 0.85f, h * 0.52f)
        )

        // 7. Bottom-Left wave
        val pathBottomLeft = Path().apply {
            moveTo(0f, h)
            lineTo(w * 0.35f, h)
            cubicTo(
                w * 0.25f, h * 0.94f,
                w * 0.08f, h * 0.93f,
                0f, h * 0.91f
            )
            close()
        }
        drawPath(path = pathBottomLeft, color = bottomWaveColor)

        // 8. Bottom-Right wave
        val pathBottomRight = Path().apply {
            moveTo(w, h)
            lineTo(w * 0.70f, h)
            cubicTo(
                w * 0.80f, h * 0.94f,
                w * 0.92f, h * 0.93f,
                w, h * 0.91f
            )
            close()
        }
        drawPath(path = pathBottomRight, color = bottomCyanColor)
    }
}
