package com.example.onlinequizapp.ui.teacher.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.onlinequizapp.data.repository.BatchRepositoryImpl
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.domain.model.Difficulty
import com.example.onlinequizapp.domain.model.Quiz
import com.example.onlinequizapp.ui.teacher.TeacherCardBg
import com.example.onlinequizapp.ui.teacher.TeacherCardBorder
import com.example.onlinequizapp.ui.teacher.TeacherTextDim
import com.example.onlinequizapp.ui.teacher.TeacherTextMuted
import com.example.onlinequizapp.ui.teacher.TeacherTextWhite
import com.example.onlinequizapp.ui.teacher.isTeacherInDarkTheme

import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.onlinequizapp.domain.model.QuizStatus
import com.example.onlinequizapp.ui.components.SubjectLogoRegistry
import com.example.onlinequizapp.ui.teacher.getCategoryGradient
import com.example.onlinequizapp.ui.teacher.getQuizLatestTimeLabel
import com.example.onlinequizapp.ui.teacher.formatTimeAgo

// -------------------------------------------------------------
// Component: Recent Quiz Card
// -------------------------------------------------------------
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
internal fun TeacherRecentQuizCard(
    quiz: Quiz,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isDark = isTeacherInDarkTheme()
    val logoRes = SubjectLogoRegistry.getDrawableRes(quiz.icon)
    val categoryGradient = getCategoryGradient(quiz.category)

    val diffLabel = quiz.difficulty.label
    val (diffColor, diffBg) = when (quiz.difficulty) {
        Difficulty.EASY -> if (isDark) Color(0xFF10B981) to Color(0xFF064E3B) else Color(0xFF059669) to Color(0xFFECFDF5)
        Difficulty.MEDIUM -> if (isDark) Color(0xFFF59E0B) to Color(0xFF451A03) else Color(0xFFD97706) to Color(0xFFFFFBEB)
        Difficulty.HARD -> if (isDark) Color(0xFFEF4444) to Color(0xFF450A0A) else Color(0xFFDC2626) to Color(0xFFFEF2F2)
    }

    val isPublished = quiz.status == QuizStatus.PUBLISHED ||
            quiz.status == QuizStatus.ACTIVE ||
            quiz.status == QuizStatus.APPROVED

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TeacherCardBg)
            .border(1.dp, TeacherCardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Real Quiz Logo or Subject Fallback
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(categoryGradient),
                contentAlignment = Alignment.Center
            ) {
                if (logoRes != null) {
                    Image(
                        painter = painterResource(id = logoRes),
                        contentDescription = quiz.title,
                        modifier = Modifier
                            .size(28.dp)
                            .padding(2.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    val lowerCat = quiz.category.lowercase()
                    when {
                        lowerCat.contains("react") || lowerCat.contains("web") -> {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = "React",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        lowerCat.contains("android") || lowerCat.contains("mobile") -> {
                            Icon(
                                imageVector = Icons.Default.Android,
                                contentDescription = "Android",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        lowerCat.contains("chem") || lowerCat.contains("science") -> {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = "Science",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        lowerCat.contains("code") || lowerCat.contains("program") -> {
                            Text(
                                text = "</>",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        else -> {
                            Text(
                                text = quiz.category.take(2).uppercase().ifBlank { "QZ" },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                val allBatchesMap by BatchRepositoryImpl.instance.getAllBatchesFlow().collectAsState(initial = emptyMap())
                val resolvedBatchNames = quiz.targetBatchIds.mapNotNull { allBatchesMap[it]?.name?.takeIf { n -> n.isNotBlank() } }

                // Title and Batch/Public Badge Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = quiz.title,
                        color = TeacherTextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Audience Badges (Only shown when quiz is PUBLISHED/ACTIVE, never for DRAFT)
                    if (quiz.status != QuizStatus.DRAFT) {
                        if (quiz.targetBatchIds.isNotEmpty()) {
                            val batchBg = if (isDark) Color(0xFF4C1D95).copy(alpha = 0.35f) else Color(0xFFF3E8FF)
                            val batchText = if (isDark) Color(0xFFC4B5FD) else Color(0xFF7C3AED)
                            val batchBorder = if (isDark) Color(0xFF7C3AED).copy(alpha = 0.5f) else Color(0xFFDDD6FE)

                            if (resolvedBatchNames.isNotEmpty()) {
                                resolvedBatchNames.forEach { bName ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(batchBg)
                                            .border(BorderStroke(0.8.dp, batchBorder), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.School,
                                                contentDescription = null,
                                                tint = batchText,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Text(
                                                text = bName,
                                                color = batchText,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(batchBg)
                                        .border(BorderStroke(0.8.dp, batchBorder), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            tint = batchText,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Text(
                                            text = "${quiz.targetBatchIds.size} Batches",
                                            color = batchText,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        } else if (quiz.targetStudentIds.isNotEmpty()) {
                            val studBg = if (isDark) Color(0xFF1E1B4B) else Color(0xFFEEF2FF)
                            val studText = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5)
                            val studBorder = if (isDark) Color(0xFF6366F1).copy(alpha = 0.5f) else Color(0xFFC7D2FE)

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(studBg)
                                    .border(BorderStroke(0.8.dp, studBorder), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = studText,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = "${quiz.targetStudentIds.size} Student${if (quiz.targetStudentIds.size > 1) "s" else ""}",
                                        color = studText,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        } else {
                            val pubBg = if (isDark) Color(0xFF0369A1).copy(alpha = 0.25f) else Color(0xFFE0F2FE)
                            val pubText = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                            val pubBorder = if (isDark) Color(0xFF0284C7).copy(alpha = 0.5f) else Color(0xFFBAE6FD)

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(pubBg)
                                    .border(BorderStroke(0.8.dp, pubBorder), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = null,
                                        tint = pubText,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = "Public for all",
                                        color = pubText,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "${quiz.questions.size} Questions • MCQ",
                        color = TeacherTextMuted,
                        fontSize = 11.5.sp
                    )

                    // Difficulty Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(diffBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = diffLabel,
                            color = diffColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Trailing status & arrow
            Column(horizontalAlignment = Alignment.End) {
                val pubBg = if (isPublished) {
                    if (isDark) Color(0xFF064E3B) else Color(0xFFECFDF5)
                } else {
                    if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                }
                val pubColor = if (isPublished) {
                    if (isDark) Color(0xFF34D399) else Color(0xFF059669)
                } else {
                    if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(pubBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isPublished) "Published" else "Draft",
                        color = pubColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                val domainStatus = when (quiz.status) {
                    QuizStatus.PUBLISHED, QuizStatus.ACTIVE, QuizStatus.APPROVED -> com.example.onlinequizapp.ui.teacher.QuizStatus.PUBLISHED
                    else -> com.example.onlinequizapp.ui.teacher.QuizStatus.DRAFT
                }
                val timeLabel = getQuizLatestTimeLabel(domainStatus, quiz.createdAt, quiz.updatedAt)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeLabel,
                        color = TeacherTextDim,
                        fontSize = 9.5.sp
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Open",
                        tint = TeacherTextDim,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
