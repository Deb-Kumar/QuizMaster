package com.example.onlinequizapp.ui.home.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.onlinequizapp.ui.components.consumeClicks
import com.example.onlinequizapp.ui.components.dismissOnClickOutside
import com.example.onlinequizapp.ui.home.LocalQuizDark

@Composable
internal fun AboutUsDialog(
    onDismiss: () -> Unit
) {
    val isDark = LocalQuizDark.current
    val dialogBg = if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val dialogBorder = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.65f) else Color(0xFFF8FAFC)
    val cardBorder = if (isDark) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

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
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(26.dp))
                    .background(dialogBg)
                    .border(1.dp, dialogBorder, RoundedCornerShape(26.dp))
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF6366F1), Color(0xFF4F46E5))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "About Us",
                                style = TextStyle(
                                    color = textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                            Text(
                                text = "QuizMaster • Version 5.0.1",
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

                // Section 1: Mission
                Text(
                    text = "OUR MISSION",
                    style = TextStyle(
                        color = Color(0xFF6366F1),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp
                    )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "QuizMaster empowers students and educators with an intelligent, engaging platform for testing knowledge, building streaks, and tracking academic progress in real-time.",
                        color = textPrimary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }

                // Section 2: Key Features
                Text(
                    text = "KEY FEATURES",
                    style = TextStyle(
                        color = Color(0xFF6366F1),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp
                    )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FeatureHighlightRow("🎯 Multi-Department Quizzes", "Hundreds of verified subjects across engineering, sciences, arts & medicine", textPrimary, textSecondary)
                    ModalDivider(color = cardBorder)
                    FeatureHighlightRow("⚡ Auto-Save & Resume", "Never lose progress if the app is closed during an active practice test", textPrimary, textSecondary)
                    ModalDivider(color = cardBorder)
                    FeatureHighlightRow("🏆 Streaks & Leaderboards", "Compete with peers and celebrate achievements every day", textPrimary, textSecondary)
                    ModalDivider(color = cardBorder)
                    FeatureHighlightRow("🌓 Adaptive Appearance", "Custom-crafted light and cyberpunk dark modes for comfortable study", textPrimary, textSecondary)
                }

                // Section 3: App Specifications
                Text(
                    text = "APP SPECIFICATIONS",
                    style = TextStyle(
                        color = Color(0xFF6366F1),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp
                    )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    InfoCardItem(
                        icon = Icons.Default.Check,
                        iconTint = Color(0xFF6366F1),
                        label = "Version",
                        value = "5.0.1 (Release Build)",
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                    ModalDivider(color = cardBorder)
                    InfoCardItem(
                        icon = Icons.Default.Android,
                        iconTint = Color(0xFF10B981),
                        label = "Platform",
                        value = "Android • Jetpack Compose UI",
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                    ModalDivider(color = cardBorder)
                    InfoCardItem(
                        icon = Icons.Default.Security,
                        iconTint = Color(0xFF3B82F6),
                        label = "Security",
                        value = "Firebase & Encrypted Local Session",
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6366F1)
                    )
                ) {
                    Text("Got It", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
internal fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    val isDark = LocalQuizDark.current
    val dialogBg = if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val dialogBorder = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.65f) else Color(0xFFF8FAFC)
    val cardBorder = if (isDark) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

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
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(26.dp))
                    .background(dialogBg)
                    .border(1.dp, dialogBorder, RoundedCornerShape(26.dp))
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF0EA5E9), Color(0xFF0284C7))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Policy,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Privacy Policy",
                                style = TextStyle(
                                    color = textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                            Text(
                                text = "Effective October 2026 • QuizMaster",
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

                // Section 1: Overview
                Text(
                    text = "DATA COLLECTION & PRIVACY",
                    style = TextStyle(
                        color = Color(0xFF0EA5E9),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp
                    )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "We prioritize your academic data security. QuizMaster collects minimal personal information—such as your name, email, university, department, course, and contact number—solely to personalize your learning journey, track quiz streaks, and maintain verified student profiles.",
                        color = textPrimary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }

                // Section 2: Data Usage
                Text(
                    text = "HOW WE USE YOUR INFORMATION",
                    style = TextStyle(
                        color = Color(0xFF0EA5E9),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp
                    )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FeatureHighlightRow(
                        "🔒 Authentication & Account Safety",
                        "Secure credentials authentication via Firebase Auth and encrypted sessions for seamless login.",
                        textPrimary,
                        textSecondary
                    )
                    ModalDivider(color = cardBorder)
                    FeatureHighlightRow(
                        "📊 Academic Performance & Streaks",
                        "Quiz responses, scores, and rankings are calculated to showcase your achievements and leaderboards.",
                        textPrimary,
                        textSecondary
                    )
                    ModalDivider(color = cardBorder)
                    FeatureHighlightRow(
                        "📞 Contact Number & Alerts",
                        "Your registered phone number is kept confidential and utilized for important security or institution alerts.",
                        textPrimary,
                        textSecondary
                    )
                    ModalDivider(color = cardBorder)
                    FeatureHighlightRow(
                        "🚫 Zero Ad-Tracking or Reselling",
                        "We never sell, rent, or trade your personal profile or academic records to third-party advertisers.",
                        textPrimary,
                        textSecondary
                    )
                }

                // Section 3: Data Protection & Rights
                Text(
                    text = "SECURITY & YOUR RIGHTS",
                    style = TextStyle(
                        color = Color(0xFF0EA5E9),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp
                    )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    InfoCardItem(
                        icon = Icons.Default.Security,
                        iconTint = Color(0xFF10B981),
                        label = "Cloud Storage",
                        value = "Cloud Firestore with Rule Enforcement",
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                    ModalDivider(color = cardBorder)
                    InfoCardItem(
                        icon = Icons.Default.Edit,
                        iconTint = Color(0xFF0EA5E9),
                        label = "Right to Update",
                        value = "Modify academic info anytime in settings",
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                    ModalDivider(color = cardBorder)
                    InfoCardItem(
                        icon = Icons.Default.DeleteForever,
                        iconTint = Color(0xFFEF4444),
                        label = "Right to Erasure",
                        value = "Delete account and all quiz history instantly",
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0EA5E9)
                    )
                ) {
                    Text("I Understand & Accept", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
internal fun FeatureHighlightRow(
    title: String,
    description: String,
    textPrimary: Color,
    textSecondary: Color
) {
    Column {
        Text(text = title, color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = description, color = textSecondary, fontSize = 11.5.sp, lineHeight = 16.sp)
    }
}
