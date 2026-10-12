package com.example.onlinequizapp.ui.home.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.onlinequizapp.domain.repository.UserStats
import com.example.onlinequizapp.ui.components.consumeClicks
import com.example.onlinequizapp.ui.components.dismissOnClickOutside
import com.example.onlinequizapp.ui.home.LocalQuizDark

private enum class AchievementCategoryTab(val label: String) {
    ALL("All"),
    QUIZZES("Quizzes"),
    CERTIFICATES("Certificates"),
    SPECIAL("Special")
}

private data class AchievementItem(
    val id: String,
    val title: String,
    val description: String,
    val category: AchievementCategoryTab,
    val current: Int,
    val target: Int,
    val isUnlocked: Boolean,
    val icon: ImageVector,
    val gradient: List<Color>
)

@Composable
internal fun AchievementsViewAllDialog(
    stats: UserStats,
    onDismiss: () -> Unit
) {
    val isDark = LocalQuizDark.current

    val dialogBg = if (isDark) Color(0xFF091030) else Color.White
    val dialogBorder = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.65f) else Color(0xFFF8FAFC)
    val cardBorder = if (isDark) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    var selectedTab by remember { mutableStateOf(AchievementCategoryTab.ALL) }

    // Build complete achievement catalogue
    val allAchievements = remember(stats) {
        listOf(
            // ── Quizzes Milestones ──────────────────────────────────────
            AchievementItem(
                id = "quiz_1",
                title = "First Quiz",
                description = "Complete your very first quiz",
                category = AchievementCategoryTab.QUIZZES,
                current = stats.quizzesTaken.coerceAtMost(1),
                target = 1,
                isUnlocked = stats.quizzesTaken >= 1,
                icon = Icons.Default.Star,
                gradient = listOf(Color(0xFF4F46E5), Color(0xFF6366F1), Color(0xFF8B5CF6))
            ),
            AchievementItem(
                id = "quiz_5",
                title = "Quiz Enthusiast",
                description = "Complete 5 quizzes successfully",
                category = AchievementCategoryTab.QUIZZES,
                current = stats.quizzesTaken.coerceAtMost(5),
                target = 5,
                isUnlocked = stats.quizzesTaken >= 5,
                icon = Icons.Default.MenuBook,
                gradient = listOf(Color(0xFF0284C7), Color(0xFF0EA5E9), Color(0xFF38BDF8))
            ),
            AchievementItem(
                id = "quiz_10",
                title = "Quiz Champion",
                description = "Complete 10 quizzes successfully",
                category = AchievementCategoryTab.QUIZZES,
                current = stats.quizzesTaken.coerceAtMost(10),
                target = 10,
                isUnlocked = stats.quizzesTaken >= 10,
                icon = Icons.Default.EmojiEvents,
                gradient = listOf(Color(0xFFD97706), Color(0xFFF59E0B), Color(0xFFFBBF24))
            ),
            AchievementItem(
                id = "quiz_20",
                title = "Quiz Master",
                description = "Complete 20 quizzes successfully",
                category = AchievementCategoryTab.QUIZZES,
                current = stats.quizzesTaken.coerceAtMost(20),
                target = 20,
                isUnlocked = stats.quizzesTaken >= 20,
                icon = Icons.Default.MilitaryTech,
                gradient = listOf(Color(0xFF7C3AED), Color(0xFF8B5CF6), Color(0xFFA78BFA))
            ),
            AchievementItem(
                id = "quiz_50",
                title = "Quiz Legend",
                description = "Reach the ultimate milestone of 50 quizzes",
                category = AchievementCategoryTab.QUIZZES,
                current = stats.quizzesTaken.coerceAtMost(50),
                target = 50,
                isUnlocked = stats.quizzesTaken >= 50,
                icon = Icons.Default.Diamond,
                gradient = listOf(Color(0xFFEC4899), Color(0xFFF43F5E), Color(0xFFFB7185))
            ),

            // ── Certificate Milestones ──────────────────────────────────
            AchievementItem(
                id = "cert_1",
                title = "First Honors",
                description = "Earn your first certificate (Score 80%+)",
                category = AchievementCategoryTab.CERTIFICATES,
                current = stats.certificatesCount.coerceAtMost(1),
                target = 1,
                isUnlocked = stats.certificatesCount >= 1,
                icon = Icons.Default.CardMembership,
                gradient = listOf(Color(0xFF059669), Color(0xFF10B981), Color(0xFF34D399))
            ),
            AchievementItem(
                id = "cert_3",
                title = "Scholar Distinction",
                description = "Earn 3 verified certificates",
                category = AchievementCategoryTab.CERTIFICATES,
                current = stats.certificatesCount.coerceAtMost(3),
                target = 3,
                isUnlocked = stats.certificatesCount >= 3,
                icon = Icons.Default.Verified,
                gradient = listOf(Color(0xFF0284C7), Color(0xFF2563EB), Color(0xFF3B82F6))
            ),
            AchievementItem(
                id = "cert_5",
                title = "Honor Roll",
                description = "Earn 5 verified certificates",
                category = AchievementCategoryTab.CERTIFICATES,
                current = stats.certificatesCount.coerceAtMost(5),
                target = 5,
                isUnlocked = stats.certificatesCount >= 5,
                icon = Icons.Default.WorkspacePremium,
                gradient = listOf(Color(0xFF6366F1), Color(0xFF7C3AED), Color(0xFF9333EA))
            ),
            AchievementItem(
                id = "cert_10",
                title = "Grand Scholar",
                description = "Achieve mastery with 10 earned certificates",
                category = AchievementCategoryTab.CERTIFICATES,
                current = stats.certificatesCount.coerceAtMost(10),
                target = 10,
                isUnlocked = stats.certificatesCount >= 10,
                icon = Icons.Default.AutoAwesome,
                gradient = listOf(Color(0xFFD97706), Color(0xFFEA580C), Color(0xFFF97316))
            ),

            // ── Special Milestones ──────────────────────────────────────
            AchievementItem(
                id = "streak_1",
                title = "Daily Fire",
                description = "Maintain a daily quiz activity streak",
                category = AchievementCategoryTab.SPECIAL,
                current = stats.dayStreak.coerceAtMost(1),
                target = 1,
                isUnlocked = stats.dayStreak >= 1,
                icon = Icons.Default.LocalFireDepartment,
                gradient = listOf(Color(0xFFEA580C), Color(0xFFF43F5E))
            ),
            AchievementItem(
                id = "century_club",
                title = "Century Club",
                description = "Score a perfect 100% on any quiz",
                category = AchievementCategoryTab.SPECIAL,
                current = if (stats.hasPerfectScore || stats.averageScorePercentage == 100) 1 else 0,
                target = 1,
                isUnlocked = stats.hasPerfectScore || stats.averageScorePercentage == 100,
                icon = Icons.Default.CheckCircle,
                gradient = listOf(Color(0xFF059669), Color(0xFF10B981))
            )
        )
    }

    val unlockedTotal = allAchievements.count { it.isUnlocked }
    val totalCount = allAchievements.size
    val progressFraction = if (totalCount > 0) unlockedTotal.toFloat() / totalCount else 0f

    val displayedAchievements = remember(selectedTab, allAchievements) {
        if (selectedTab == AchievementCategoryTab.ALL) {
            allAchievements
        } else {
            allAchievements.filter { it.category == selectedTab }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .dismissOnClickOutside(onDismiss)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .consumeClicks()
                    .fillMaxWidth()
                    .fillMaxHeight(0.88f)
                    .clip(RoundedCornerShape(26.dp))
                    .background(dialogBg)
                    .border(1.dp, dialogBorder, RoundedCornerShape(26.dp))
            ) {
                // Header Row (Fixed): Trophy Icon + Title + Close Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "All Achievements",
                                style = TextStyle(
                                    color = textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                            Text(
                                text = "Track your quiz milestones & badges",
                                style = TextStyle(
                                    color = textSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                ModalDivider(color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))

                // Scrollable Content Area: Takes weight(1f)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                // Overall Progress Summary Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "🏆 Overall Progress",
                                    color = textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF2563EB).copy(alpha = 0.15f))
                                    .border(1.dp, Color(0xFF2563EB).copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "$unlockedTotal of $totalCount Unlocked (${(progressFraction * 100).toInt()}%)",
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF38BDF8),
                            trackColor = if (isDark) Color(0xFF0F172A) else Color(0xFFE2E8F0)
                        )
                    }
                }

                // Category Filter Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AchievementCategoryTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        val count = if (tab == AchievementCategoryTab.ALL) {
                            allAchievements.size
                        } else {
                            allAchievements.count { it.category == tab }
                        }
                        val pillBg = if (isSelected) {
                            Color(0xFF2563EB)
                        } else {
                            if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                        }
                        val pillBorder = if (isSelected) {
                            Color(0xFF3B82F6)
                        } else {
                            if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
                        }
                        val pillText = if (isSelected) Color.White else textSecondary

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(pillBg)
                                .border(1.dp, pillBorder, RoundedCornerShape(10.dp))
                                .clickable { selectedTab = tab }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${tab.label} ($count)",
                                color = pillText,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Achievement Cards List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    displayedAchievements.forEach { achievement ->
                        AchievementDetailCard(
                            item = achievement,
                            cardBg = cardBg,
                            cardBorder = cardBorder,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            isDark = isDark
                        )
                    }
                }

                }

                ModalDivider(color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))

                // Fixed Bottom Action Bar: Always fully visible and properly spaced!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB)
                        )
                    ) {
                        Text(
                            text = "Close",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementDetailCard(
    item: AchievementItem,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    isDark: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Squircle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        if (item.isUnlocked) {
                            Brush.linearGradient(item.gradient)
                        } else {
                            Brush.linearGradient(
                                if (isDark) {
                                    listOf(Color(0xFF131B38), Color(0xFF0F172A))
                                } else {
                                    listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))
                                }
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.isUnlocked) item.icon else Icons.Default.Lock,
                    contentDescription = item.title,
                    tint = if (item.isUnlocked) Color.White else (if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title + Description + Mini Progress
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
                Text(
                    text = item.description,
                    color = textSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                // Progress Bar for incomplete tiered achievements
                if (!item.isUnlocked && item.target > 1) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val progressRatio = (item.current.toFloat() / item.target).coerceIn(0f, 1f)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color(0xFF38BDF8),
                            trackColor = if (isDark) Color(0xFF0F172A) else Color(0xFFE2E8F0)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${item.current}/${item.target}",
                            color = textSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Status Pill (Unlocked vs Locked)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (item.isUnlocked) {
                            Color(0xFF10B981).copy(alpha = 0.15f)
                        } else {
                            if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                        }
                    )
                    .border(
                        1.dp,
                        if (item.isUnlocked) {
                            Color(0xFF10B981).copy(alpha = 0.35f)
                        } else {
                            if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
                        },
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (item.isUnlocked) "✓ Unlocked" else "🔒 Locked",
                    style = TextStyle(
                        color = if (item.isUnlocked) Color(0xFF34D399) else textSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp
                    )
                )
            }
        }
    }
}
