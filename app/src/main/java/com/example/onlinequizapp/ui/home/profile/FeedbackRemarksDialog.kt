package com.example.onlinequizapp.ui.home.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.onlinequizapp.domain.model.User
import com.example.onlinequizapp.ui.components.consumeClicks
import com.example.onlinequizapp.ui.components.dismissOnClickOutside
import com.example.onlinequizapp.ui.components.showToast
import com.example.onlinequizapp.ui.home.LocalQuizDark

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FeedbackRemarksDialog(
    user: User?,
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, category: String, comment: String, onResult: (Boolean) -> Unit) -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalQuizDark.current

    val dialogBg = if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val dialogBorder = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.65f) else Color(0xFFF8FAFC)
    val cardBorder = if (isDark) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    var selectedRating by remember { mutableIntStateOf(5) }
    var selectedCategory by remember { mutableStateOf("General Opinion") }
    var commentText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    val categories = listOf(
        "General Opinion",
        "Problem / Bug",
        "Quiz Questions",
        "Feature Request",
        "UI / Design"
    )

    val ratingLabels = mapOf(
        1 to "Needs Improvement 😞",
        2 to "Fair / Okay 😐",
        3 to "Good Experience 🙂",
        4 to "Very Good! 😊",
        5 to "Outstanding & Excellent! 🌟"
    )

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .dismissOnClickOutside { if (!isSubmitting) onDismiss() }
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .consumeClicks()
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(26.dp))
                    .background(dialogBg)
                    .border(1.dp, dialogBorder, RoundedCornerShape(26.dp))
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RateReview,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Feedback & Remarks",
                                style = TextStyle(
                                    color = textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.5.sp
                                )
                            )
                            Text(
                                text = "Share your rating, thoughts & issues",
                                style = TextStyle(
                                    color = textSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // 1. Interactive Star Rating Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "HOW WOULD YOU RATE QUIZMASTER?",
                        style = TextStyle(
                            color = Color(0xFFF59E0B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp
                        )
                    )

                    // 5 Stars Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (starIndex in 1..5) {
                            val isSelected = starIndex <= selectedRating
                            val starScale by animateFloatAsState(
                                targetValue = if (isSelected) 1.15f else 1.0f,
                                animationSpec = tween(durationMillis = 200),
                                label = "star_scale"
                            )
                            val starColor by animateColorAsState(
                                targetValue = if (isSelected) Color(0xFFF59E0B) else textSecondary.copy(alpha = 0.4f),
                                animationSpec = tween(durationMillis = 200),
                                label = "star_color"
                            )

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .scale(starScale)
                                    .clip(CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        selectedRating = starIndex
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.Star else Icons.Outlined.StarOutline,
                                    contentDescription = "Star $starIndex",
                                    tint = starColor,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    // Rating description
                    Text(
                        text = ratingLabels[selectedRating] ?: "Good",
                        style = TextStyle(
                            color = textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                // 2. Category Selector Chips
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "TOPIC / CATEGORY",
                        style = TextStyle(
                            color = Color(0xFF8B5CF6),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp
                        )
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            val isChosen = selectedCategory == cat
                            val chipBg = if (isChosen) Color(0xFF8B5CF6) else cardBg
                            val chipBorder = if (isChosen) Color(0xFF8B5CF6) else cardBorder
                            val chipTextColor = if (isChosen) Color.White else textSecondary

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(chipBg)
                                    .border(1.dp, chipBorder, RoundedCornerShape(12.dp))
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = cat,
                                    style = TextStyle(
                                        color = chipTextColor,
                                        fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }

                // 3. Multiline Input Field for Opinion / Problem
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "YOUR OPINION OR PROBLEM",
                        style = TextStyle(
                            color = Color(0xFF3B82F6),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp
                        )
                    )

                    OutlinedTextField(
                        value = commentText,
                        onValueChange = {
                            if (it.length <= 600) {
                                commentText = it
                                if (isError && it.isNotBlank()) isError = false
                            }
                        },
                        placeholder = {
                            Text(
                                text = "Describe what you like, suggestions to improve, or any problems and difficulties you encountered...",
                                color = textSecondary.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        },
                        isError = isError,
                        minLines = 4,
                        maxLines = 7,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = cardBorder,
                            focusedContainerColor = cardBg,
                            unfocusedContainerColor = cardBg
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AnimatedVisibility(visible = isError) {
                            Text(
                                text = "Please enter your opinion or problem before sending.",
                                color = Color(0xFFEF4444),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        if (!isError) {
                            Spacer(modifier = Modifier.width(1.dp))
                        }
                        Text(
                            text = "${commentText.length} / 600",
                            color = textSecondary.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 4. Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.7f) else Color(0xFFF1F5F9),
                            contentColor = if (isDark) textSecondary else Color(0xFF475569)
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            val trimmed = commentText.trim()
                            if (trimmed.isBlank()) {
                                isError = true
                                return@Button
                            }

                            isSubmitting = true
                            onSubmit(selectedRating, selectedCategory, trimmed) { success ->
                                isSubmitting = false
                                if (success) {
                                    context.showToast("Thank you for your feedback! ✨")
                                    onDismiss()
                                } else {
                                    context.showToast("Failed to submit feedback. Please try again.")
                                }
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color(0xFF64748B).copy(alpha = 0.5f)
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Send Feedback",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
