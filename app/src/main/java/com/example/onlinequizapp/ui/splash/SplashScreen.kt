package com.example.onlinequizapp.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.R
import com.example.onlinequizapp.ui.theme.ThemeManager
import com.example.onlinequizapp.ui.theme.ThemeMode
import kotlinx.coroutines.delay

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
 * Modern App Opening / Splash Screen
 * Features:
 * - Subtle ambient radial & vertical gradient background (Theme-aware)
 * - Elevated Squircle App Icon with scale-in & glow
 * - App Name ("QuizMaster") with smooth slide-up animation (down to up)
 * - Subtitle ("Learn. Practice. Conquer.") with smooth slide-up and fade-in
 * - 3 animated bouncing/wave dots at the bottom
 * - "Preparing your app..." loading text
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier,
    displayDurationMillis: Long = 2400L
) {
    val isDark = isAppInDarkTheme()
    var startAnimation by remember { mutableStateOf(false) }

    // Trigger animations on entrance
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(displayDurationMillis)
        onSplashFinished()
    }

    // 1. Logo Scale & Alpha Animation
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.65f,
        animationSpec = tween(
            durationMillis = 750,
            easing = FastOutSlowInEasing
        ),
        label = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "logoAlpha"
    )

    // 2. App Name Slide-Up (Down-to-Up) & Fade-In Animation
    val nameOffsetY by animateDpAsState(
        targetValue = if (startAnimation) 0.dp else 42.dp,
        animationSpec = tween(
            durationMillis = 800,
            delayMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "nameOffsetY"
    )
    val nameAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 700,
            delayMillis = 200
        ),
        label = "nameAlpha"
    )

    // 3. Subtitle Slide-Up & Fade-In Animation
    val subtitleOffsetY by animateDpAsState(
        targetValue = if (startAnimation) 0.dp else 26.dp,
        animationSpec = tween(
            durationMillis = 800,
            delayMillis = 380,
            easing = FastOutSlowInEasing
        ),
        label = "subtitleOffsetY"
    )
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 750,
            delayMillis = 380
        ),
        label = "subtitleAlpha"
    )

    // Color definitions
    val bgGradient = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF080614),
                Color(0xFF130D2B),
                Color(0xFF090618)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF7F4FF),
                Color(0xFFEDE5FE),
                Color(0xFFF8F5FF)
            )
        )
    }

    val ambientGlowColor = if (isDark) {
        Color(0xFF7C3AED).copy(alpha = 0.28f)
    } else {
        Color(0xFFDDD2FE).copy(alpha = 0.70f)
    }

    val titleColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF1E1B4B)
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val loadingTextColor = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
    val dotColor = Color(0xFF7C3AED)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        // Ambient Radial Glow behind the center logo
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ambientGlowColor, Color.Transparent),
                    center = Offset(size.width / 2f, size.height * 0.45f),
                    radius = size.width * 0.65f
                ),
                radius = size.width * 0.65f,
                center = Offset(size.width / 2f, size.height * 0.45f)
            )
        }

        // Center Content: Logo, Name (Down-to-Up), Subtitle
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. App Icon Squircle
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = Color(0xFF7C3AED).copy(alpha = 0.45f)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF7C3AED),
                                Color(0xFF6366F1),
                                Color(0xFF4338CA)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Check if custom app logo is present, otherwise fallback to graduation icon
                val hasCustomLogo = remember {
                    try {
                        R.drawable.app_logo != 0
                    } catch (_: Exception) {
                        false
                    }
                }

                if (hasCustomLogo) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "QuizMaster Logo",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "QuizMaster Logo",
                        tint = Color.White,
                        modifier = Modifier.size(58.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 2. App Name (Animated: Down to Up)
            Row(
                modifier = Modifier
                    .offset(y = nameOffsetY)
                    .alpha(nameAlpha),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quiz",
                    style = TextStyle(
                        color = titleColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 34.sp,
                        letterSpacing = (-0.5).sp
                    )
                )
                Text(
                    text = "Master",
                    style = TextStyle(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF7C3AED), Color(0xFFA855F7))
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 34.sp,
                        letterSpacing = (-0.5).sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Subtitle (Animated: Down to Up & Fade)
            Text(
                text = "Learn. Practice. Conquer.",
                style = TextStyle(
                    color = subtitleColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.3.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .offset(y = subtitleOffsetY)
                    .alpha(subtitleAlpha)
            )
        }

        // Bottom Section: Animated 3 Dots & Loading Message
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 54.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated 3 Bouncing Wave Dots
            AnimatedThreeDotsLoader(dotColor = dotColor)

            Spacer(modifier = Modifier.height(14.dp))

            // Loading Text
            Text(
                text = "Preparing your app...",
                style = TextStyle(
                    color = loadingTextColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.2.sp
                )
            )
        }
    }
}

/**
 * 3 Animated Bouncing Dots with wave offset effect
 */
@Composable
private fun AnimatedThreeDotsLoader(
    dotColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dotsLoader")

    // Dot 1 bounce
    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -9f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 900
                0f at 0
                -9f at 220 with FastOutSlowInEasing
                0f at 440 with FastOutSlowInEasing
                0f at 900
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot1Offset"
    )

    // Dot 2 bounce (staggered delay)
    val offset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -9f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 900
                0f at 0
                0f at 140
                -9f at 360 with FastOutSlowInEasing
                0f at 580 with FastOutSlowInEasing
                0f at 900
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot2Offset"
    )

    // Dot 3 bounce (staggered delay)
    val offset3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -9f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 900
                0f at 0
                0f at 280
                -9f at 500 with FastOutSlowInEasing
                0f at 720 with FastOutSlowInEasing
                0f at 900
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot3Offset"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dot 1
        Box(
            modifier = Modifier
                .offset(y = offset1.dp)
                .size(8.5.dp)
                .clip(CircleShape)
                .background(dotColor)
        )

        // Dot 2
        Box(
            modifier = Modifier
                .offset(y = offset2.dp)
                .size(8.5.dp)
                .clip(CircleShape)
                .background(dotColor)
        )

        // Dot 3
        Box(
            modifier = Modifier
                .offset(y = offset3.dp)
                .size(8.5.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
    }
}
