package com.example.onlinequizapp.ui.home.certificates

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.onlinequizapp.domain.model.CertificateSummaryStats
import com.example.onlinequizapp.domain.model.EarnedCertificateItem
import com.example.onlinequizapp.domain.model.QuizToUnlockCertificateItem
import com.example.onlinequizapp.ui.components.QuizVisualHelper
import com.example.onlinequizapp.ui.components.UserAvatar
import com.example.onlinequizapp.ui.components.consumeClicks
import com.example.onlinequizapp.ui.components.dismissOnClickOutside
import com.example.onlinequizapp.ui.theme.ThemeManager
import com.example.onlinequizapp.ui.theme.ThemeMode
import com.example.onlinequizapp.util.CertificateGenerator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─────────────────────────────────────────────────────────────
// DESIGN PALETTE
// ─────────────────────────────────────────────────────────────
private val BrandIndigo = Color(0xFF6366F1)
private val BrandIndigoLight = Color(0xFF818CF8)
private val BrandIndigoDark = Color(0xFF4F46E5)
private val GoldAccent = Color(0xFFF59E0B)
private val GoldDark = Color(0xFFD97706)
private val EmeraldSuccess = Color(0xFF10B981)
private val PurpleDistinction = Color(0xFF8B5CF6)

@Composable
fun CertificatesScreen(
    onBackClick: () -> Unit,
    onRetakeQuizClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CertificatesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val themeMode by ThemeManager.themeMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemDark
    }

    val pageBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val cardBg = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(pageBg),
        containerColor = pageBg,
        topBar = {
            CertificatesTopBar(
                onBackClick = onBackClick,
                onRefreshClick = { viewModel.refresh() },
                isDark = isDark,
                textPrimary = textPrimary,
                textSecondary = textSecondary
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = BrandIndigo,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Loading your certificates...",
                            fontSize = 13.5.sp,
                            color = textSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Header Overview Summary
                    item {
                        CertificatesHeaderBanner(
                            stats = uiState.summaryStats,
                            studentName = uiState.studentName,
                            studentEmail = uiState.studentEmail,
                            avatarUrl = uiState.avatarUrl,
                            isDark = isDark,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary
                        )
                    }

                    // 2. Tab Navigation (Earned vs Unlock More)
                    item {
                        CertificatesTabBar(
                            selectedTab = uiState.selectedTab,
                            earnedCount = uiState.earnedCertificates.size,
                            unlockCount = uiState.quizzesToUnlock.size,
                            onTabSelect = { viewModel.selectTab(it) },
                            isDark = isDark,
                            cardBg = cardBg,
                            cardBorder = cardBorder,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary
                        )
                    }

                    // 3. Tab Contents
                    if (uiState.selectedTab == 0) {
                        if (uiState.earnedCertificates.isEmpty()) {
                            item {
                                EmptyCertificatesCard(
                                    onExploreClick = { viewModel.selectTab(1) },
                                    isDark = isDark,
                                    cardBg = cardBg,
                                    cardBorder = cardBorder,
                                    textPrimary = textPrimary,
                                    textSecondary = textSecondary
                                )
                            }
                        } else {
                            items(uiState.earnedCertificates) { cert ->
                                EarnedCertificateCard(
                                    certificate = cert,
                                    onPreviewClick = { viewModel.selectCertificateForPreview(cert) },
                                    onDownloadClick = { downloadOrOpenCertificate(context, cert) },
                                    onShareClick = { shareCertificate(context, cert) },
                                    isDark = isDark,
                                    cardBg = cardBg,
                                    cardBorder = cardBorder,
                                    textPrimary = textPrimary,
                                    textSecondary = textSecondary
                                )
                            }
                        }
                    } else {
                        if (uiState.quizzesToUnlock.isEmpty()) {
                            item {
                                AllUnlockedCard(
                                    isDark = isDark,
                                    cardBg = cardBg,
                                    cardBorder = cardBorder,
                                    textPrimary = textPrimary,
                                    textSecondary = textSecondary
                                )
                            }
                        } else {
                            items(uiState.quizzesToUnlock) { item ->
                                QuizToUnlockCard(
                                    item = item,
                                    onRetakeClick = { onRetakeQuizClick(item.quizId) },
                                    isDark = isDark,
                                    cardBg = cardBg,
                                    cardBorder = cardBorder,
                                    textPrimary = textPrimary,
                                    textSecondary = textSecondary
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    // Digital Diploma In-App Realistic Preview Modal
    uiState.selectedCertificateForPreview?.let { cert ->
        CertificatePreviewDialog(
            certificate = cert,
            onDismiss = { viewModel.selectCertificateForPreview(null) },
            onDownload = { downloadOrOpenCertificate(context, cert) },
            onShare = { shareCertificate(context, cert) },
            isDark = isDark
        )
    }
}

// ─────────────────────────────────────────────────────────────
// TOP APP BAR
// ─────────────────────────────────────────────────────────────
@Composable
private fun CertificatesTopBar(
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit,
    isDark: Boolean,
    textPrimary: Color,
    textSecondary: Color
) {
    val barBg = if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val barBorder = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)

    Surface(
        color = barBg,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textPrimary
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Certificates & Honors",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Verified credentials & achievement awards",
                            fontSize = 11.5.sp,
                            color = textSecondary
                        )
                    }
                }

                IconButton(onClick = onRefreshClick) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = textSecondary
                    )
                }
            }
            HorizontalDivider(color = barBorder, thickness = 0.8.dp)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// HEADER OVERVIEW BANNER
// ─────────────────────────────────────────────────────────────
@Composable
private fun CertificatesHeaderBanner(
    stats: CertificateSummaryStats,
    studentName: String,
    studentEmail: String,
    avatarUrl: String?,
    isDark: Boolean,
    textPrimary: Color,
    textSecondary: Color
) {
    val bannerGradient = Brush.linearGradient(
        colors = if (isDark) {
            listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF1E293B))
        } else {
            listOf(Color(0xFFEEF2FF), Color(0xFFE0E7FF), Color(0xFFF1F5F9))
        }
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = BrandIndigo.copy(alpha = 0.35f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .background(bannerGradient)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profile & Credential Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                UserAvatar(
                    avatarUrl = avatarUrl,
                    name = studentName,
                    size = 48.dp,
                    borderWidth = 2.dp,
                    borderColor = GoldAccent
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = studentName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldAccent.copy(alpha = 0.18f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Verified Scholar",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldDark
                            )
                        }
                    }
                    if (studentEmail.isNotBlank()) {
                        Text(
                            text = studentEmail,
                            fontSize = 12.sp,
                            color = textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            HorizontalDivider(color = BrandIndigo.copy(alpha = 0.15f), thickness = 0.8.dp)

            // 3 Stat Counters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CertSummaryStatPill(
                    icon = Icons.Default.WorkspacePremium,
                    iconTint = GoldAccent,
                    label = "Total Earned",
                    value = "${stats.totalEarned}",
                    sub = "80%+ score",
                    modifier = Modifier.weight(1f),
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                CertSummaryStatPill(
                    icon = Icons.Default.Star,
                    iconTint = PurpleDistinction,
                    label = "Distinction",
                    value = "${stats.distinctionCount}",
                    sub = "90%+ score",
                    modifier = Modifier.weight(1f),
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                CertSummaryStatPill(
                    icon = Icons.Default.CheckCircle,
                    iconTint = BrandIndigo,
                    label = "Merit Awards",
                    value = "${stats.meritCount}",
                    sub = "80-89% score",
                    modifier = Modifier.weight(1f),
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )
            }
        }
    }
}

@Composable
private fun CertSummaryStatPill(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    sub: String,
    modifier: Modifier = Modifier,
    textPrimary: Color,
    textSecondary: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(iconTint.copy(alpha = 0.1f))
            .border(0.8.dp, iconTint.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )
            Text(
                text = label,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = sub,
                fontSize = 9.sp,
                color = textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// TAB BAR (Earned vs Unlock More)
// ─────────────────────────────────────────────────────────────
@Composable
private fun CertificatesTabBar(
    selectedTab: Int,
    earnedCount: Int,
    unlockCount: Int,
    onTabSelect: (Int) -> Unit,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = cardBg,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = BrandIndigo,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = BrandIndigo,
                    height = 3.dp
                )
            },
            divider = {}
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { onTabSelect(0) },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = if (selectedTab == 0) GoldAccent else textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Earned ($earnedCount)",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.5.sp,
                            color = if (selectedTab == 0) textPrimary else textSecondary
                        )
                    }
                }
            )

            Tab(
                selected = selectedTab == 1,
                onClick = { onTabSelect(1) },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (selectedTab == 1) BrandIndigo else textSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Unlock More ($unlockCount)",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.5.sp,
                            color = if (selectedTab == 1) textPrimary else textSecondary
                        )
                    }
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// EARNED CERTIFICATE CARD
// ─────────────────────────────────────────────────────────────
@Composable
private fun EarnedCertificateCard(
    certificate: EarnedCertificateItem,
    onPreviewClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onShareClick: () -> Unit,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    val visuals = QuizVisualHelper.getVisuals(
        title = certificate.quizTitle,
        category = certificate.quizCategory,
        iconKey = certificate.quizIcon
    )

    val gradeColor = if (certificate.isDistinction) PurpleDistinction else BrandIndigo
    val gradeBg = gradeColor.copy(alpha = 0.12f)

    val formattedDate = if (certificate.submittedAtMillis > 0) {
        SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(certificate.submittedAtMillis))
    } else {
        "Verified"
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.2.dp,
                color = if (certificate.isDistinction) GoldAccent.copy(alpha = 0.5f) else cardBorder,
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Row: Real Quiz Logo + Title & Grade Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(visuals.gradientBrush),
                        contentAlignment = Alignment.Center
                    ) {
                        QuizVisualHelper.QuizLogoEmblem(
                            visuals = visuals,
                            contentDescription = certificate.quizTitle,
                            iconSize = 26.dp,
                            iconTint = Color.White
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = certificate.quizTitle,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${certificate.quizCategory} • Issued $formattedDate",
                            fontSize = 11.5.sp,
                            color = textSecondary
                        )
                    }
                }

                // Score pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(EmeraldSuccess.copy(alpha = 0.15f))
                        .border(1.dp, EmeraldSuccess.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${certificate.scorePercentage}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess
                    )
                }
            }

            // Middle Row: Grade Tag + Credential ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(gradeBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (certificate.isDistinction) Icons.Default.Star else Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = gradeColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = certificate.gradeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = gradeColor
                        )
                    }
                }

                Text(
                    text = "ID: ${certificate.certificateId}",
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = textSecondary
                )
            }

            HorizontalDivider(color = cardBorder, thickness = 0.8.dp)

            // Action Buttons: Preview, Download PDF, Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onPreviewClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BrandIndigo.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Preview",
                        tint = BrandIndigo,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Preview",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandIndigo
                    )
                }

                Button(
                    onClick = onDownloadClick,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldDark),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download PDF",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Download PDF",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Certificate",
                        tint = textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// QUIZ TO UNLOCK CARD
// ─────────────────────────────────────────────────────────────
@Composable
private fun QuizToUnlockCard(
    item: QuizToUnlockCertificateItem,
    onRetakeClick: () -> Unit,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    val visuals = QuizVisualHelper.getVisuals(
        title = item.quizTitle,
        category = item.quizCategory,
        iconKey = item.quizIcon
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(visuals.gradientBrush),
                        contentAlignment = Alignment.Center
                    ) {
                    QuizVisualHelper.QuizLogoEmblem(
                        visuals = visuals,
                        contentDescription = item.quizTitle,
                        iconSize = 24.dp,
                        iconTint = Color.White
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = item.quizTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (item.hasAttempted) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Previous: ${item.previousScore}%",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GoldDark
                            )
                            Text(
                                text = "• Need +${item.gapPercentage}% for Certificate",
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                        }
                    } else {
                        Text(
                            text = "${item.questionsCount} Questions • Score 80%+ to unlock",
                            fontSize = 11.5.sp,
                            color = textSecondary
                        )
                    }
                }
            }

            Button(
                onClick = onRetakeClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (item.hasAttempted) "Retake" else "Start",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// EMPTY STATES
// ─────────────────────────────────────────────────────────────
@Composable
private fun EmptyCertificatesCard(
    onExploreClick: () -> Unit,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(GoldAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = "No Certificates Earned Yet",
                fontSize = 16.5.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )

            Text(
                text = "Score 80% or higher on any qualifying quiz to earn an official verified certificate of achievement!",
                fontSize = 13.sp,
                color = textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Button(
                onClick = onExploreClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text("Explore Available Quizzes", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AllUnlockedCard(
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = EmeraldSuccess,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "All Quizzes Certified! 🌟",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )
            Text(
                text = "You have earned certificates for all currently assigned quizzes. Excellent job!",
                fontSize = 13.sp,
                color = textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// DIGITAL DIPLOMA IN-APP PREVIEW MODAL
// ─────────────────────────────────────────────────────────────
@Composable
private fun CertificatePreviewDialog(
    certificate: EarnedCertificateItem,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    isDark: Boolean
) {
    val context = LocalContext.current
    val dateText = remember(certificate.submittedAtMillis) {
        if (certificate.submittedAtMillis > 0) {
            SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date(certificate.submittedAtMillis))
        } else {
            SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date())
        }
    }

    val certBitmap = remember(certificate) {
        CertificateGenerator.generateCertificateBitmap(
            context = context,
            studentName = certificate.studentName,
            quizName = certificate.quizTitle,
            score = certificate.scorePercentage.toDouble(),
            category = certificate.quizCategory,
            certificateId = certificate.certificateId,
            dateMillis = certificate.submittedAtMillis
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .dismissOnClickOutside(onDismiss)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF),
                shadowElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .consumeClicks()
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(listOf(GoldAccent, GoldDark, Color(0xFFFDE68A))),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Modal Header with Close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Official Digital Diploma",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                        }
                    }

                    // Certificate Graphic Frame
                    if (certBitmap != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    width = 1.5.dp,
                                    color = GoldAccent.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = certBitmap.asImageBitmap(),
                                contentDescription = "Certificate Preview",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(2048f / 1448f),
                                contentScale = ContentScale.Fit
                            )
                        }
                    } else {
                        // Fallback Graphic Frame
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFF0A192F),
                                            Color(0xFF0F2B48),
                                            Color(0xFF0A192F)
                                        )
                                    )
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = GoldAccent.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "★ QUIZMASTER ACADEMY ★",
                                    fontSize = 10.sp,
                                    letterSpacing = 2.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent
                                )

                                Text(
                                    text = "CERTIFICATE OF ACHIEVEMENT",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )

                                Text(
                                    text = "This is proudly presented to",
                                    fontSize = 11.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = Color(0xFF94A3B8)
                                )

                                Text(
                                    text = certificate.studentName,
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFDE68A),
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "for demonstrating subject mastery and excellence in",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFFCBD5E1),
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = certificate.quizTitle,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GoldAccent.copy(alpha = 0.25f))
                                        .border(1.dp, GoldAccent, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 14.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Final Score: ${certificate.scorePercentage}% • ${certificate.gradeText}",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFDE68A)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text(
                                            text = "Category: ${certificate.quizCategory}",
                                            fontSize = 9.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                        Text(
                                            text = "Date: $dateText",
                                            fontSize = 9.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                        Text(
                                            text = "ID: ${certificate.certificateId}",
                                            fontSize = 8.5.sp,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "✍️ Academic Board",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = GoldAccent
                                        )
                                        Text(
                                            text = "Verified Credential",
                                            fontSize = 8.5.sp,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Action buttons: Download PDF + Share
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onShare,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share")
                        }

                        Button(
                            onClick = onDownload,
                            modifier = Modifier
                                .weight(1.4f)
                                .height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldDark)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download PDF",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download PDF", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// HELPERS FOR PDF DOWNLOAD & SHARING
// ─────────────────────────────────────────────────────────────
private fun downloadOrOpenCertificate(context: Context, item: EarnedCertificateItem) {
    val certFile = CertificateGenerator.generateCertificate(
        context = context,
        studentName = item.studentName,
        quizName = item.quizTitle,
        score = item.scorePercentage.toDouble(),
        category = item.quizCategory,
        certificateId = item.certificateId,
        dateMillis = item.submittedAtMillis
    )

    if (certFile != null) {
        Toast.makeText(context, "Certificate generated: ${certFile.name}", Toast.LENGTH_SHORT).show()
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                certFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Open Certificate"))
        } catch (_: Exception) {
            Toast.makeText(context, "Certificate saved to: ${certFile.absolutePath}", Toast.LENGTH_LONG).show()
        }
    } else {
        Toast.makeText(context, "Could not generate certificate", Toast.LENGTH_SHORT).show()
    }
}

private fun shareCertificate(context: Context, item: EarnedCertificateItem) {
    val certFile = CertificateGenerator.generateCertificate(
        context = context,
        studentName = item.studentName,
        quizName = item.quizTitle,
        score = item.scorePercentage.toDouble(),
        category = item.quizCategory,
        certificateId = item.certificateId,
        dateMillis = item.submittedAtMillis
    )

    if (certFile != null) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                certFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Certificate of Achievement - ${item.quizTitle}")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "I scored ${item.scorePercentage}% in ${item.quizTitle} and earned an official Certificate of Achievement on QuizMaster! (Credential ID: ${item.certificateId})"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Certificate"))
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share certificate", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(context, "Could not prepare certificate for sharing", Toast.LENGTH_SHORT).show()
    }
}

