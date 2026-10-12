package com.example.onlinequizapp.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.onlinequizapp.domain.model.Quiz

/**
 * Visual styling and iconography for quizzes across the app.
 * Ensures identical logos, colors, and gradients on:
 * - Home Page (Available Quizzes & Resume Quiz Card)
 * - My Quizzes Page (All / In Progress / Completed lists)
 * - Quiz Start / Preview Modal
 * - Resume Quiz Modal
 * - Student History Page (Quiz attempt list)
 */
data class QuizVisuals(
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val primaryColor: Color,
    @DrawableRes val logoDrawableRes: Int? = null
) {
    val gradientBrush: Brush
        get() = Brush.linearGradient(gradientColors)
}

object QuizVisualHelper {

    fun getVisuals(title: String, category: String = "", iconKey: String? = null): QuizVisuals {
        val resolvedLogoRes = resolveLogoDrawable(iconKey, title, category)

        val query = "$title $category".lowercase()
        val baseVisuals = when {
            query.contains("science") -> QuizVisuals(
                icon = Icons.Default.Science,
                gradientColors = listOf(Color(0xFF2563EB), Color(0xFF06B6D4)),
                primaryColor = Color(0xFF06B6D4)
            )
            query.contains("mathematics") || query.contains("math") || query.contains("calculate") -> QuizVisuals(
                icon = Icons.Default.Calculate,
                gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFEA580C)),
                primaryColor = Color(0xFFF59E0B)
            )
            query.contains("general knowledge") || query.contains("trivia") || query.contains("gk") -> QuizVisuals(
                icon = Icons.Default.Public,
                gradientColors = listOf(Color(0xFF10B981), Color(0xFF059669)),
                primaryColor = Color(0xFF10B981)
            )
            query.contains("chemistry") || query.contains("biotech") || query.contains("chem") -> QuizVisuals(
                icon = Icons.Default.Biotech,
                gradientColors = listOf(Color(0xFFF43F5E), Color(0xFFE11D48)),
                primaryColor = Color(0xFFF43F5E)
            )
            query.contains("android") -> QuizVisuals(
                icon = Icons.Default.Android,
                gradientColors = listOf(Color(0xFF059669), Color(0xFF10B981)),
                primaryColor = Color(0xFF10B981)
            )
            query.contains("react") -> QuizVisuals(
                icon = Icons.Default.Code,
                gradientColors = listOf(Color(0xFF06B6D4), Color(0xFF3B82F6)),
                primaryColor = Color(0xFF38BDF8)
            )
            query.contains("javascript") || query.contains("code") || query.contains("programming") || query.contains("python") || query.contains("kotlin") -> QuizVisuals(
                icon = Icons.Default.Code,
                gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF6366F1)),
                primaryColor = Color(0xFF818CF8)
            )
            query.contains("history") || query.contains("culture") || query.contains("literature") || query.contains("book") -> QuizVisuals(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)),
                primaryColor = Color(0xFFA855F7)
            )
            else -> QuizVisuals(
                icon = Icons.Default.Psychology,
                gradientColors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
                primaryColor = Color(0xFF818CF8)
            )
        }

        return baseVisuals.copy(logoDrawableRes = resolvedLogoRes)
    }

    fun getVisuals(quiz: Quiz): QuizVisuals = getVisuals(quiz.title, quiz.category, quiz.icon)

    private fun resolveLogoDrawable(iconKey: String?, title: String, category: String): Int? {
        // 1. Explicit icon key matching
        val explicitMatch = SubjectLogoRegistry.getDrawableRes(iconKey)
        if (explicitMatch != null) return explicitMatch

        // 2. Intelligent fallback from title/category matching registered logos
        val lowerTitle = title.lowercase()
        val lowerCategory = category.lowercase()
        return SubjectLogoRegistry.logos.firstOrNull { logo ->
            logo.id != "default" && (
                lowerTitle.contains(logo.id) ||
                lowerTitle.contains(logo.name.lowercase()) ||
                lowerCategory.contains(logo.id) ||
                lowerCategory.contains(logo.name.lowercase())
            )
        }?.drawableRes
    }

    /**
     * Reusable composable to render the quiz emblem (real subject drawable or vector icon fallback)
     */
    @Composable
    fun QuizLogoEmblem(
        visuals: QuizVisuals,
        contentDescription: String?,
        modifier: Modifier = Modifier,
        iconSize: Dp = 26.dp,
        iconTint: Color = Color.White
    ) {
        if (visuals.logoDrawableRes != null) {
            Image(
                painter = painterResource(id = visuals.logoDrawableRes),
                contentDescription = contentDescription,
                modifier = modifier.size(iconSize),
                contentScale = ContentScale.Fit
            )
        } else {
            Icon(
                imageVector = visuals.icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = modifier.size(iconSize)
            )
        }
    }
}
